package actors.batchProcessor

import akka.actor.{Actor, ActorRef, FSM}
import akka.util.Timeout
import models.batch.Batch
import models.batch.OperationStatus._
import models.batch.job.{DataSetOp, MineOp}
import models.messages.batchProcessing.{SubmitDsOpJob, SubmitMineJob}
import models.messages.logger.Log

import scala.concurrent.duration._

/**
 * Created by basso on 10/04/15.
 *
 * Batch worker
 * responsible for a single user
 *
 *
 * Logging responsiblity:
 *  Just Log the status of the Batch,
 *  individual jobs will be managed by the sub-systems
 *
 * Invariant: Will not work for Batches with Empty Job list
 */

object Worker {
  trait State
  object Available extends State    // The worker is available for a new batch
  object Busy extends State         // The worker is busy with a current batch

  trait Data
  object Uninitialized extends Data
  case class Work(batch: Batch) extends Data

  object NextJob
}
import actors.batchProcessor.Worker._

/**
 * Method:
 *  The Current Job is the batch.jobs.head
 */
class Worker (val user: String, val mediator: ActorRef)
  extends Actor with FSM[State, Data] {

  implicit val timeout = Timeout(3 seconds)   // Forgot what this was for

  startWith(Available, Uninitialized)

  when (Available) {
      /** Batch submission */
    case Event(b: Batch, Uninitialized) =>
      self ! NextJob
      goto(Busy) using Work(b)
  }

  when (Busy) {
      /** The current job has succeded, move on */
    case Event(OpSuccess | OpWarning, Work(b)) =>
      self ! NextJob
      stay using Work(b.copy(jobs = b.jobs.tail))       // Here we update the jobs

      /** The current job has failed. Fail the batch */
    case Event(OpFailure, Work(b)) =>
      mediator ! Log(OpFailure, user, "The job "+b.jobs.head.id+" failed", b)
      goto(Available) using Uninitialized

      /** Actual algo to move to next job in batch */
    case Event(NextJob, Work(batch)) => batch.jobs match {      // Called every time to move onto the next job
      case Nil =>
        mediator ! Log(OpSuccess, user, "The batch completed successfully", batch)
        goto(Available) using Uninitialized
      case job :: rest =>
        job match {
          case j: DataSetOp => mediator ! SubmitDsOpJob(user, j)
          case j: MineOp => mediator ! SubmitMineJob(user, j)
          case _ => throw new Exception("Unknown job type")
        }
        stay // The current job will be the head of the list
    }
  }

    /** Every time a Batch finishes, inform the controller that it is free */
  onTransition {
    case Busy -> Available => context.parent ! IAmFree(user)
  }

}
