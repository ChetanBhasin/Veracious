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
import models.messages.persistenceManaging.DataSetEntry
import models.mining.mlret._
import models.mining.{Algorithm, MinerResult}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.sys.process._


/**
 * Created by chetan on 13/04/15.
 */
class WorkerActor(mediator: ActorRef) extends Actor {

  private implicit val timeout = Timeout(10 seconds)

  /**
   * Handles output operations of the mining results
   * @param operation Details of user and dataset wrapped in GetDsData case object Details of user and dataset wrapped in GetDsData case object Details of user and dataset wrapped in GetDsData case object Details of user and dataset wrapped in GetDsData case object
   * @param dsm Actor reference to datastore manager
   * @return Option[JSON-Output]
   */
  private def handleDsOutput(operation: GetDsData, dsm: ActorRef) = {
    val (uname, name) = (operation.username, operation.Ds)
    val result = Await.result(dsm ? CheckUserDataset(uname, name), 10 seconds).asInstanceOf[Option[DataSetEntry]]
    val filepath = s"./datastore/datasets/$uname/$name"
    result match {
      case Some(x: DataSetEntry) => x.targetAlgorithm match {
        case "als" => new RALS(filepath, name).output
        case "clustering" => new RClustering(filepath, name).output
        case "fpm" => new RFPM(filepath, name).output
        case "svm" => new RSVM(filepath, name).output
        case _ => None
      }
      case None => None
    }
  }

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
    case DsAddFromUrl(name, desc, target_algo, url, id) =>
      try {
        val downloader = new URL(url) #> new File(s"./datasets/$username/$name")
        downloader.run()
        OperationStatus.OpSuccess
      } catch {
        case _: Throwable => OperationStatus.OpFailure
      }

    /**
     * Add a file directly from the supplied resource
     */
    case DsAddDirect(name, desc, target_algo, file, id) =>
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

    /**
     * Removes a dataset on a user's account
     */
    case DsDelete(name, id) =>
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

    /**
     * Refreshes the file from the original download location
     */
    case DsRefresh(name, id) =>
      try {
        val url = dsm ? GiveUserData(username) match {
          case x: Future[Any] => Await.result(x, 30 seconds) match {
            case DataSetEntry(_, _, _, _, _, url: String) => url
          }
        }
        val downloader = new URL(url) #> new File(s"./datastore/datasetes/$username/$name")
        downloader.run()
        OperationStatus.OpSuccess
      } catch {
        case _: Throwable => OperationStatus.OpFailure
      }

    case _ => OperationStatus.OpFailure
  }

  def receive = {

    /**
     * Manages all the incoming Dataset related jobs
     *
     * Modified by Anish,
     * The reply should be a JobStatus message
     */
    case (dsm: ActorRef, SubmitDsOpJob(username: String, job: DataSetOp)) => handleDsJob(username, job, dsm) match {
      case s@OperationStatus.OpSuccess =>
        sender ! JobStatus(username, s)
        mediator ! Log(s, username, "Dataset stored on disk successfully.", job)

      case s@OperationStatus.OpWarning =>
        sender ! JobStatus(username, s)
        mediator ! Log(s, username, "Warning: No fatal error, but operation could not be completed.", job)

      case s@OperationStatus.OpFailure =>
        sender ! JobStatus(username, s)
        mediator ! Log(s, username, "Fatal error: Operation could not be completed", job)
    }

    /**
     * Save the incoming miner result to the disk
     */
    case MinerResult(al: Algorithm.Algorithm, user: String, name: String, save: (String => Unit), job) => {
      val dsdir = Paths.get(s"./datastore/")
      val dssdir = Paths.get(s"./datastore/datasets/")
      val userdir = Paths.get(s"./datastore/datasets/$user")
      try {
        if (!Files.exists(dsdir)) Files.createDirectory(dsdir)
        if (!Files.exists((dssdir))) Files.createDirectories((dssdir))
        if (!Files.exists(userdir)) Files.createDirectories(userdir)
        save(s"./datastore/datasets/$user/$name.dat")
        mediator ! Log(OperationStatus.OpSuccess, user, "The mine operation was a success", job)
        mediator ! JobStatus(user, OperationStatus.OpSuccess)
      } catch {
        case _: Throwable =>
          println("Something went wrong.") //Log here
          mediator ! Log(OperationStatus.OpFailure, user, "The mine operation failed, could'nt write to disk", job)
          mediator ! JobStatus(user, OperationStatus.OpFailure)
      }
    }

    /**
     * Manage the request for mine operation results
     */
    case (operation: GetDsData, dsm: ActorRef) => handleDsOutput(operation, dsm)

  }

}
