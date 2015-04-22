package actors.application

import actors.batchProcessor.BatchProcessor
import actors.client.ClientManager
import actors.logger.Logger
import actors.mediator.{Mediator, RegisterForNotification}
import actors.miner.Miner
import actors.persistenceManager.Persistence
import akka.actor._
import models.messages.{Ready, SysError}

import scala.collection.mutable.{Map => mMap}

/**
 * Created by basso on 15/04/15.
 */

object ApplicationManager {
  val moduleList = List(
    classOf[BatchProcessor],
    classOf[Logger],
    classOf[Miner],
    classOf[Persistence],
    classOf[ClientManager]
  )

  trait AppData
  case class ModuleSetup (modules: Set[(Class[_], ActorRef)]) extends AppData
  case class Modules (modules: Map[String, ActorRef]) extends AppData

  trait AppControl
  /*object StartSetup extends AppControl        // TODO: We can possibly send in the configuration using this */
  object Shutdown extends AppControl
}

import actors.application.ApplicationManager._

class ApplicationManager extends Actor
with FSM[AppState, AppData] with ActorLogging {

  val mediator = context.actorOf(Props[Mediator])
  mediator ! RegisterForNotification (self)

  moduleList foreach { cls =>
    context.actorOf ( Props(cls, mediator), cls.getSimpleName )
    // context watch
    // TODO: have to manage the supervisor strategy
  }

  startWith (AppSetup, ModuleSetup (Set()))

  when (AppSetup) {
    case Event ( Ready(cls), ModuleSetup(set) ) =>
      val nSet = set + ((cls, sender))
      if ( nSet.size == moduleList.length )
        goto (AppRunning) using Modules (
          nSet map { case (k,v) => (k.getSimpleName, v) } toMap
        )
      else stay using ModuleSetup (nSet)
  }

  /*
  when (AppRunning) {
    // TODO: handle AppShutdown by creating a safe shutdown procedure
  }*/

  // TODO: External actor needs to subscribe to state change notification

  whenUnhandled {
    case Event (SysError(susys, msg), _) =>
      log.error("Subsystem: "+susys+" Send error message: "+msg)
      stay()
  }

}
