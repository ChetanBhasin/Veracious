package actors.batchProcessor

import akka.actor.{ActorRef, ActorRefFactory, Props}
import models.batch.Batch

import scala.collection.mutable.{Queue => mQueue}

/**
 * Objects of this class form individual records of the workers
 * They will be mapped against the username of the client
 * @param worker The actorRef to the worker
 * @param queue A queue of Batches left for the given worker
 * @param workerFree True if the worker is ready for another Batch
 * @param userLoggedIn True if the user is active
 */
case class WorkerRecord (worker: ActorRef, queue: mQueue[Batch], workerFree: Boolean, userLoggedIn: Boolean)

object WorkerRecord {
  /* Overloaded */
  def apply(user: String, mediator: ActorRef)(implicit actorFactory: ActorRefFactory): WorkerRecord = {
    WorkerRecord(
      actorFactory.actorOf(Props(classOf[Worker], user, mediator), "batchWorker:" + user),
      mQueue[Batch](),
      workerFree = true, userLoggedIn = true
    )
  }
}
