package actors.persistenceManager

import java.io.File
import java.net.URL
import java.nio.file.{Files, Paths}

import akka.actor.{Actor, ActorRef}
import akka.pattern.ask
import akka.util.Timeout
import models.batch.job._
import models.messages.batchProcessing._
import models.messages.persistenceManaging.EnterDataSet

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.sys.process._


/**
 * Created by chetan on 13/04/15.
 */
class WorkerActor(mediator: ActorRef) extends Actor {

  private implicit val timeout = Timeout(10 seconds)

  private def handleDsJob(username: String, job: DataSetOp, dsm: ActorRef) = job match {

    /**
     * Download a file from a source
     */
    case DsAddFromUrl(name, desc, target_algo, url, id) => {
      try {
        val downloader = new URL(url) #> new File(s"./datasets/$username/$name")
        downloader.run()
      } catch {
        case _: Throwable => println("Got something unexpected")
      }
    }

    /**
     * Add a file directly from the supplied resource
     */
    case DsAddDirect(name, desc, target_algo, file, id) => {
      try {
        val filepath = Paths.get(s"./datastore/datsets/$username/$name")
        if (Files.exists(filepath)) throw new Exception("A file with name already exists")
        else file.moveTo(new File(s"./datastore/datasets/$username/$name"))
      } catch {
        case _: Throwable => println("Got something unexpected")
      }
    }
    case DsDelete(name, id) => {
      val filepath = Paths.get(s"./datastore/datasets/$username/$name")
      if (!Files.exists(filepath)) {
        dsm ! RemoveDatasetRecord(username, name)
        Files.delete(filepath)
      }
    }
    case DsRefresh(name, id) => {
      try {
        val url = (dsm ? GiveUserData(username)) match {
          case x: Future[Any] => Await.result(x, 30 seconds) match {
            case EnterDataSet(_, _, _, _, url: String) => url
          }
        }
        val downloader = new URL(url) #> new File(s"./datastore/datasetes/$username/$name")
        downloader.run()
      } catch {
        case _: Throwable => println("Got something unexpected")
      }
    }

    case _ => println("Unhandled message")
  }

  def receive = {
    case (dsm: ActorRef, SubmitDsOpJob(username: String, job: DataSetOp)) => handleDsJob(username, job, dsm)
  }

}
