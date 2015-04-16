package actors.persistenceManager

import java.io.File
import java.net.URL
import java.nio.file.{Files, Paths}

import akka.actor.{Actor, ActorRef}
import akka.pattern.ask
import akka.util.Timeout
import models.batch.OperationStatus
import models.batch.job._
import models.messages.batchProcessing._
import models.messages.logger.Log
import models.messages.persistenceManaging.EnterDataSet
import models.mining
import models.mining.MinerResult

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.sys.process._


/**
 * Created by chetan on 13/04/15.
 */
class WorkerActor(mediator: ActorRef) extends Actor {

  private implicit val timeout = Timeout(10 seconds)

  /**
   * handles all the jobs for the actor reciever
   * @param username username to work on
   * @param job job to be dealt with
   * @param dsm dataset manager actor reference
   * @return OperationStatus
   */
  private def handleDsJob(username: String, job: DataSetOp, dsm: ActorRef) = job match {

    /**
     * Download a file from a source
     */
    case DsAddFromUrl(name, desc, target_algo, url, id) => {
      try {
        val downloader = new URL(url) #> new File(s"./datasets/$username/$name")
        downloader.run()
        OperationStatus.OpSuccess
      } catch {
        case _: Throwable => OperationStatus.OpFailure
      }
    }

    /**
     * Add a file directly from the supplied resource
     */
    case DsAddDirect(name, desc, target_algo, file, id) => {
      try {
        val filepath = Paths.get(s"./datastore/datsets/$username/$name")
        if (Files.exists(filepath)) OperationStatus.OpWarning
        else {
          file.moveTo(new File(s"./datastore/datasets/$username/$name"))
          OperationStatus.OpSuccess
        }
      } catch {
        case _: Throwable => OperationStatus.OpFailure
      }
    }

    /**
     * Removes a dataset on a user's account
     */
    case DsDelete(name, id) => {
      val filepath = Paths.get(s"./datastore/datasets/$username/$name")
      try {
        if (!Files.exists(filepath)) {
          dsm ! RemoveDatasetRecord(username, name)
          Files.delete(filepath)
          OperationStatus.OpSuccess
        } else OperationStatus.OpWarning
      } catch {
        case _: Throwable => OperationStatus.OpFailure
      }
    }

    /**
     * Refreshes the file from the original download location
     */
    case DsRefresh(name, id) => {
      try {
        val url = (dsm ? GiveUserData(username)) match {
          case x: Future[Any] => Await.result(x, 30 seconds) match {
            case EnterDataSet(_, _, _, _, url: String) => url
          }
        }
        val downloader = new URL(url) #> new File(s"./datastore/datasetes/$username/$name")
        downloader.run()
        OperationStatus.OpSuccess
      } catch {
        case _: Throwable => OperationStatus.OpFailure
      }
    }

    case _ => OperationStatus.OpFailure
  }

  def receive = {

    /**
     * Manages all the incoming Dataset related jobs
     */
    case (dsm: ActorRef, SubmitDsOpJob(username: String, job: DataSetOp)) => handleDsJob(username, job, dsm) match {
      case OperationStatus.OpSuccess => {
        sender ! OperationStatus.OpSuccess
        mediator ! Log(OperationStatus.OpSuccess, username, "Dataset stored on disk successfully.", job)
      }
      case OperationStatus.OpWarning => {
        sender ! OperationStatus.OpWarning
        mediator ! Log(OperationStatus.OpSuccess, username, "Warning: No fatal error, but operation could not be completed.", job)
      }
      case OperationStatus.OpFailure => {
        sender ! OperationStatus.OpFailure
        mediator ! Log(OperationStatus.OpFailure, username, "Fatal error: Operation could not be completed", job)
      }
    }

  }

}
