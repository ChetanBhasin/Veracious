package actors.batchProcessor

import actors.application.AppModule
import actors.mediator._
import akka.actor.{ActorRef, PoisonPill}
import models.messages.application.{FinishWork, FinishedWork}
import models.messages.batchProcessing._
import models.messages.client.{LogIn, LogOut}

import scala.collection.mutable.{Map => mMap}

class BatchProcessor (val mediator: ActorRef) extends AppModule {

  mediator ! RegisterForReceive (self, classOf[BatchProcessorMessage])
  val workerTable = mMap[String, WorkerRecord]()
  implicit val actorFactory = context

  var _finishingStage = false

  def receive = {
    /**
     * The LogIn & LogOut messages are sent by the ClientManager to help this
     * module set up batch workers for each client
     */
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

    /** ------------------------------------------------- **/

    /**
     * Actual batch processing
     * 1. Submit batch
     * 2. Get back a JobStatus for each job
     * 3. Once a batch is finished, the worker sends an IAmFree message
     */
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
          context stop act

          // Step 2 of FinishWork
          if (_finishingStage && workerTable.isEmpty) mediator ! FinishedWork

        } else workerTable.update(user, wr.copy(workerFree = true)) // user still logged in
      case None =>
        moduleError("Impossible, worker without record")
    }
    /** ------------------------------------------------- **/

    /**
     * FinishWork sent by the application controller as part of
     * the safe shutdown procedure.
     *
     * It will wait for the batch processor workers to finish all the jobs
     * and then will carry out the shutdown for the rest of the system
     *
     * At this point, the client-manager will have disconnected all clients
     * and the login controls will be unavailabe, so we can safely
     * assume no new logins
     */
    case FinishWork =>
      // Step 1, assume every client has logged out
      workerTable.foreach {
        case (user, wr @ WorkerRecord(_, _, _, true)) =>
          workerTable.update(user, wr.copy(userLoggedIn = false))
        case _ => Unit
      }
      // ready for Step 2,
      _finishingStage = true
  }
}
