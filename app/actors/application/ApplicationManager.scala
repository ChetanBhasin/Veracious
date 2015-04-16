package actors.application

import actors.batchProcessor.BatchProcessor
import actors.logger.Logger
import actors.miner.Miner
import actors.persistenceManager.Persistence

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

  trait AppData
  object Uninitialised extends AppData
}
/*
import ApplicationManager._

class ApplicationManager extends Actor with FSM[AppState, AppData] {

  val mediator = context.actorOf(Props[Mediator])
  mediator ! RegisterForNotification (self)
  // Do something about receiving SysErrors

  startWith (AppSetup, Uninitialised)

  when (
}
*/
