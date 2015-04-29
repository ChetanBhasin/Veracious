package actors.application

import actors.batchProcessor.BatchProcessor
import actors.client.ClientManager
import actors.logger.Logger
import actors.mediator.RegisterForReceive
import actors.miner.Miner
import actors.persistenceManager.Persistence
import akka.actor.SupervisorStrategy.Resume
import akka.actor._
import models.messages.application._
import models.messages.client.UserAlreadyLoggedIn
import models.messages.persistenceManaging.GetUserManager

import scala.collection.mutable.{Map => mMap}

/**
 * Created by basso on 15/04/15.
 *
 * This is the main parent actor. It manages all the main modules of the system and
 * is responsible for a safe shutdown, along with system level logging
 */

object ApplicationManager {
  /** The list of Main modules of the application*/
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

class ApplicationManager (val mediator: ActorRef) extends Actor
with FSM[AppState, AppData] with ActorLogging {

  mediator ! RegisterForReceive (self, classOf[AppControl])

  /** Create all the main modules */
  moduleList foreach { cls =>
    context.actorOf ( Props(cls, mediator), cls.getSimpleName )
    // context watch
  }

  /** The main modules should not restart. but rather resume */
  override val supervisorStrategy = OneForOneStrategy() {
    case _: Throwable => Resume
    //case _: Exception => Resume
  }

  startWith (AppSetup, ModuleSetup (Set()))

  when (AppSetup) {
    /** Every module will send a Ready message on start,
      * We count the ready messages and make sure every module is up and running
      * before we go into the AppRunning state where we accept incoming connections
      */
    case Event ( Ready(cls), ModuleSetup(set) ) =>
      val nSet = set + ((cls, sender))
      if ( nSet.size == moduleList.length )
        goto (AppRunning) using Modules (
          nSet map { case (k,v) => (k.getSimpleName, v) } toMap
        )
      else stay using ModuleSetup (nSet)
  }

  /**
   * This is the State when the application accepts incoming connections from clients
   */
  when (AppRunning) {
    /**
     * AppShutDown is issued by the appAccess proxy.
     * We begin the safe shutdown procedure now by sending FinishWork to the mediator
     * which will be received by the BatchProcessor and Logger respectively
     * and we go to the AppFinish state
     */
    case Event (AppShutDown, Modules(mods)) =>
      mediator ! FinishWork
      goto (AppFinish) using Finishing(mods, 0)

    /**
     * The AppAccess will demand a userManager when it faces incoming connections. This userManager
     * has been coded by @Chetan and manages the user authentication features of the application
     */
    case Event (GetUserManager, _) =>
      mediator ! ((GetUserManager, sender))
      stay

    /**
     * Check if the user has already logged in, will be accepted by the client manager and
     * will eventually return Boolean
     */
    case Event (u @ UserAlreadyLoggedIn(user), _) =>
      mediator ! ((u, sender))
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

  /**
   * Last stage of the app. Here we wait for the safe shutdown procedure to finish
   * and then commit suicide.
   */
  when (AppFinish) {
    /** The first module (of either batch processor or logger) has finished and is ready to die*/
    case Event (FinishedWork, Finishing(m, 0)) =>
      stay using Finishing(m, 1)

    /** The second module is done, so we systematically kill all modules and then SUICIDE */
    case Event (FinishedWork, Finishing(mods, 1)) =>
      mods.values.foreach { _ ! PoisonPill }
      context stop self
      stay
  }

  /** Handling sysError messages */
  whenUnhandled {
    case Event (SysError(susys, msg), _) =>
      log.error("Subsystem: "+susys+" Send error message: "+msg)
      stay()
  }

}
