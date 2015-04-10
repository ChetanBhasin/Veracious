package actors.batchProcessor

import actors.mediator._
import akka.actor.{Actor, ActorRef, PoisonPill}
import models.messages._
import models.messages.batchProcessing._
import models.messages.client.{LogIn, LogOut}

import scala.collection.mutable.{Map => mMap}

/**
 * Created by basso on 10/04/15.
 */
class BatchProcessor (val mediator: ActorRef) extends Actor {

  mediator ! RegisterForReceive (self, classOf[BatchProcessorMessage])
  val workerTable = mMap[String, WorkerRecord]()
  implicit val actorFactory = context

  override def preStart() {
    mediator ! Ready("BatchProcessor")      // TODO: context.parent
  }

  override def postStop() {
    mediator ! Unregister(self)
  }

  def receive = {
    case LogIn(user) => workerTable.get(user) match {
      case Some(wr@WorkerRecord(_, _, wf, false)) => workerTable.update(user, wr.copy(userLoggedIn = true))
      case None => workerTable.update(user, WorkerRecord(user, mediator))
      case Some(WorkerRecord(_,_,_,true)) => throw new Exception("Logged in user submitting fresh LogIn")
    }

    case LogOut(user) => workerTable.get(user) match {
      case Some(wr @ WorkerRecord(act, que, wf, ul)) =>
        if (que.nonEmpty || !wf) workerTable.update(user, wr.copy(userLoggedIn = false))
        else {
          act ! PoisonPill
          workerTable -= user
        }
      case None => throw new Exception("Worker for user never created")
    }

    case SubmitBatch(user, batch) => workerTable.get(user) match {
      case Some(WorkerRecord(act, que, wf, ul)) =>
        if (!wf) que.enqueue(batch)
        else act ! batch
      case None => throw new Exception("Batch came for unknown user")
    }

    case JobStatus(user, status) => workerTable.get(user) match {
      case Some(WorkerRecord(act, _, _, _)) => act ! status           // Find the worker and send him the status
      case None => throw new Exception("Job status for missing worker")
    }

    case IAmFree(user) => workerTable.get(user) match {
      case Some(wr @ WorkerRecord (act, que, _, ul)) =>
        if (que.nonEmpty) act ! que.dequeue()           // worker is available and so is a new Batch, submit
        else if (!ul) {                                 // user is not logged in
          act ! PoisonPill
          workerTable -= user
        } else workerTable.update(user, wr.copy(workerFree = true))   // user still logged in
      case None => throw new Exception("Impossible, worker without record")
    }
  }
}
