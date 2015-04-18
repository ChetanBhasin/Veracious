package actors.application

import actors.batchProcessor.BatchProcessor
import actors.logger.Logger
import actors.mediator.{Mediator, RegisterForNotification}
import actors.miner.Miner
import actors.persistenceManager.Persistence
import akka.actor._
import models.messages.Ready

import scala.collection.mutable.{Map => mMap}

/**
 * Created by basso on 15/04/15.
 */

object ApplicationManager {
  val moduleList = List(
    classOf[BatchProcessor],
    classOf[Logger],
    classOf[Miner],
    classOf[Persistence]
  )
  case class ModSetupRecord (module: ActorRef, ready: Boolean)

  trait AppData
  //object Uninitialised extends AppData
  case class ModuleSetup (modules: Map[Class[_], ModSetupRecord]) extends AppData
  case class Modules (modules: Map[Class[_], ActorRef]) extends AppData

  /*trait AppControl
  object StartSetup extends AppControl        // TODO: We can possibly send in the configuration using this */
}

import actors.application.ApplicationManager._

class ApplicationManager extends Actor
with FSM[AppState, AppData] with ActorLogging {

  val mediator = context.actorOf(Props[Mediator])
  mediator ! RegisterForNotification (self)
  // Do something about receiving SysErrors

  startWith (AppSetup, ModuleSetup( modules =
    moduleList zip ( moduleList map { cls =>
      ModSetupRecord (
        context.actorOf (Props(cls, mediator), cls.getSimpleName),
        ready = false
      )
    }) toMap
  ))

  when (AppSetup) {
    case Event (Ready(cls), ms @ ModuleSetup(modules)) =>
      if (!modules.contains(cls)) throw new Exception("Ghost sub-system")
      else {
        val newMod = modules.updated (cls, modules(cls).copy(ready = true))
        if ( newMod.forall { case (k, ModSetupRecord(_, rdy)) => rdy } )
          goto (AppRunning) using Modules (
            modules mapValues { case ModSetupRecord(act,_) => act }
          )
        else stay using ms.copy(modules = newMod)
      }
  }

  // TODO: handle SysError
}
