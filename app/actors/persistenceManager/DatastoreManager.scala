package actors.persistenceManager

import java.nio.file.{StandardOpenOption, Files, Paths}

import models.messages.persistenceManaging.datasetEntry
import akka.actor.{Props, ActorSystem, Actor}
import models.mining.Algorithm.{ALS, Clustering, FPgrowth, SVM}
import play.api.libs.json.{JsObject, JsString}
import play.libs.Json

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.Source

import java.nio.file

case class GiveUserData(username: String)

case class AddDatasetRecord(username: String, dataset: datasetEntry)

/**
 * Created by chetan on 14/04/15.
 */
object DatastoreManager {

  /**
   * Turn a line of string into datasetEntry case class
   * @param line line of text
   * @return
   */
  def makeDsEntry(line: String) = {
    line split ("::") match {
      case Array(name: String, dtype: String, targetAlgo: String, status: String) =>
        datasetEntry(name, dtype, targetAlgo, status)
      case _ => throw new Error("Got something of which I have no idea.")
    }
  }


  /**
   * Turn an incoming datasetEntry case class into a line of text to be written in the file
   * @param incoming Entry data
   * @return
   */
  def makeEntryText(incoming: datasetEntry): String = {
    case datasetEntry(name, dtype, targetAlgo, status) => s"$name::$dtype::$targetAlgo::$status"
  }

  // Check on weather a single value exists or not
  var singleton = true

  /**
   * Returns Typed Actor of type DatastoreManager
   * @param system ActorSystem of under which the actor should be created.
   * @return
   */
  def apply(system: ActorSystem) = {
    if (singleton) {
      singleton = false
      val actorProps = Props(new DatastoreManager)
      system.actorOf(actorProps)
    } else {
      throw new Exception("Only one object at a time is allowed.")
    }
  }
}

/**
 * Class meant to br produced as a typed actor
 */
class DatastoreManager extends Actor {

  private def getUserDatasets(uname: String) = Future {
    val stream = Source.fromFile(s"./datastore/meta/usersets/$uname.dat")
    val vals = for {
      item <- stream.getLines
      formatted <- DatastoreManager.makeDsEntry(item)
    } yield formatted
    stream.close()
    vals
  }

  private def addUserDataset(uname: String, dataset: datasetEntry) = {
    val filePath = Paths.get(s"./datastore/meta/usersets/$uname.dat")
    if (!Files.exists(filePath)) Files.createFile(filePath)
    try {
      Files.write(filePath, DatastoreManager.makeEntryText(dataset).getBytes, StandardOpenOption.APPEND)
    }
  }

  def receive = {
    case GiveUserData(username: String) => sender ! getUserDatasets(username)
    case AddDatasetRecord(username: String, data: datasetEntry) => addUserDataset(username, data)
  }

}
