package actors.batchProcessor

import actors.application.AppModule
import actors.mediator._
import akka.actor.{ActorRef, PoisonPill}
import models.messages.batchProcessing._
import models.messages.client.{LogIn, LogOut}

import scala.collection.mutable.{Map => mMap}

class BatchProcessor (val mediator: ActorRef) extends AppModule {

  mediator ! RegisterForReceive (self, classOf[BatchProcessorMessage])
  val workerTable = mMap[String, WorkerRecord]()
  implicit val actorFactory = context

  /**
   * Exceptions are going to stop the BatchProcessor,. Need to find some other
   * way to propagate errors
   */

  def receive = {
    case LogIn(user) => workerTable.get(user) match {
      case Some(wr @ WorkerRecord(_, _, wf, false)) => workerTable.update(user, wr.copy(userLoggedIn = true))
      case None => workerTable.update(user, WorkerRecord(user, mediator))
      case Some(WorkerRecord(_,_,_,true)) => moduleError("Logged in user submitting fresh LogIn")
    }

    case LogOut(user) => workerTable.get(user) match {
      case Some(wr @ WorkerRecord(act, que, wf, ul)) =>
        if (que.nonEmpty || !wf) workerTable.update(user, wr.copy(userLoggedIn = false))
        else {
          workerTable -= user
          act ! PoisonPill
        }
      case None =>
        moduleError("Worker for user never created")
    }

    case SubmitBatch(user, batch) => workerTable.get(user) match {
      case Some(wr @ WorkerRecord(act, que, wf, ul)) =>
        if (!wf)
          que.enqueue(batch)
        else {
          workerTable.put(user, wr.copy(workerFree = false))
          act ! batch
        }

      case None =>
        moduleError("Batch came for unknown user")
    }

    case JobStatus(user, status) => workerTable.get(user) match {
      case Some(WorkerRecord(act, _, _, _)) => act ! status           // Find the worker and send him the status
      case None => moduleError("Job status for missing worker")
    }

    case IAmFree(user) => workerTable.get(user) match {
      case Some(wr @ WorkerRecord (act, que, _, ul)) =>
        if (que.nonEmpty) {
          act ! que.dequeue() // worker is available and so is a new Batch, submit
          //workerTable.update(user, wr.copy(workerFree = false))
        } else if (!ul) {                                 // user is not logged in
          workerTable -= user
          act ! PoisonPill
        } else workerTable.update(user, wr.copy(workerFree = true)) // user still logged in
      case None => moduleError("Impossible, worker without record")
    }
  }
}
