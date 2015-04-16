package actors.application

import actors.batchProcessor.BatchProcessor
import actors.logger.Logger
import actors.miner.Miner

import scala.collection.mutable.{Map => mMap}

/**
 * Created by basso on 15/04/15.
 */

object ApplicationManager {
  val childList = List(
    classOf[BatchProcessor],
    classOf[Logger],
    classOf[Miner]
  )
}


