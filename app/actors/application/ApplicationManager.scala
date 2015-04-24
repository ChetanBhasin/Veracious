package actors.application

import actors.batchProcessor.BatchProcessor
import actors.client.ClientManager
import actors.logger.Logger
import actors.mediator.{Mediator, RegisterForReceive}
import actors.miner.Miner
import actors.persistenceManager.Persistence
import akka.actor.SupervisorStrategy.Resume
import akka.actor._
import models.messages.application._
import models.messages.persistenceManaging.GetUserManager

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

  sealed trait AppData
  private case class ModuleSetup (modules: Set[(Class[_], ActorRef)]) extends AppData
  private case class Modules (modules: Map[String, ActorRef]) extends AppData
  private case class Finishing(modules: Map[String, ActorRef], num: Int) extends AppData

}

import actors.application.ApplicationManager._

class ApplicationManager extends Actor
with FSM[AppState, AppData] with ActorLogging {

  val mediator = context.actorOf(Props[Mediator])
  mediator ! RegisterForReceive (self, classOf[AppControl])

  moduleList foreach { cls =>
    context.actorOf ( Props(cls, mediator), cls.getSimpleName )
    // context watch
  }

  override val supervisorStrategy = OneForOneStrategy() {
    case _: Throwable => Resume
    //case _: Exception => Resume
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

  when (AppRunning) {
    case Event (AppShutDown, Modules(mods)) =>
      mediator ! FinishWork
      goto (AppFinish) using Finishing(mods, 0)

    case Event (GetUserManager, _) =>
      mediator ! ((GetUserManager, sender))
      stay
  }

  when (AppFinish) {
    case Event (FinishedWork, Finishing(m, 0)) =>
      stay using Finishing(m, 1)
    case Event (FinishedWork, Finishing(mods, 1)) =>
      mods.values.foreach { _ ! PoisonPill }
      context stop self
      stay
  }

  /**
   * Safe shutdown procedure:
   *  1. Send messages to Batch Processor and ClientManager to finish up their work
   *    1.1 The Batch processor will wait for any worker still doing it's job
   *    1.2 The Client Manager will disconnect all the clients
   *  2. Go to AppFinish
   *  3. Once these two reply with confirmations, then do graceful stop of all the modules
   */

  // TODO: External actor needs to subscribe to state change notification

  whenUnhandled {
    case Event (SysError(susys, msg), _) =>
      log.error("Subsystem: "+susys+" Send error message: "+msg)
      stay()
  }

}
