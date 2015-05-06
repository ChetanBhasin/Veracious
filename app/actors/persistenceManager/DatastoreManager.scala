package actors.persistenceManager

import java.nio.file.{Files, Paths, StandardOpenOption}

import akka.actor.{Actor, ActorSystem, Props}
import models.messages.persistenceManaging.DataSetEntry

import scala.io.Source

case class GiveUserData(username: String)

case class GiveRawUserData(username: String)

case class AddDatasetRecord(username: String, dataset: DataSetEntry)

case class RemoveDatasetRecord(username: String, dataset: String)

case class ModifyDatasetStatus(username: String, dataset: DataSetEntry, newStatus: String)

case class RemoveUserEntirely(username: String)

case class CheckUserDataset(username: String, ds: String)

/**
 * Created by chetan on 14/04/15.
 */


/**
 * Companion object for the DatastoreManager actor
 */
object DatastoreManager {

  /**
   * Turn a line of string into datasetEntry case class
   * @param line line of text
   * @return
   */
  def makeDsEntry(line: String) = {
    line split ("::") match {
      case Array(name: String, desc: String, dtype: String, targetAlgo: String, status: String, source: String) =>
        DataSetEntry(name, desc, dtype, targetAlgo, status, source)
      case _ => throw new Error("Got something of which I have no idea.")
    }
  }


  /**
   * Turn an incoming datasetEntry case class into a line of text to be written in the file
   * @param incoming Entry data
   * @return
   */
  def makeEntryText(incoming: DataSetEntry): String = incoming match {
    case DataSetEntry(name, desc, dtype, targetAlgo, status, source) => s"$name::$desc::$dtype::$targetAlgo::$status::$source"
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

import models.jsonWrites._
import play.api.libs.json._
/**
 * Class meant to br produced as a typed actor
 */
class DatastoreManager extends Actor {

  /**
   * Modified by Anish
   * Get a JsArray of All the data-sets belonging to the user
   *
   */
  private def getUserDatasets(uname: String): JsValue = try {
    val stream = Source.fromFile(s"./.datastore/meta/usersets/$uname.dat")
    if (stream isEmpty) JsNull
    else {
      val vals = stream.getLines.map {
        line => DatastoreManager.makeDsEntry(line)
      }
      stream.close()
      Json.toJson(vals.toSeq)   // Should work automagically with the JsonWrites available for DataSetEntry
    }
  } catch {
    case ex: Throwable => JsNull
  }

  /**
   * Get Raw records of datasets owned by a user
   * @param uname
   * @return
   */
  private def getRawUserDatasets(uname: String): Iterator[DataSetEntry] = try {
    val stream = Source.fromFile(s"./.datastore/meta/usersets/$uname.dat")
    val vals: Iterator[DataSetEntry] = stream.getLines().map(DatastoreManager.makeDsEntry(_))
    stream.close()
    vals
  } catch {
    case _: Throwable => Iterator(Nil.asInstanceOf[DataSetEntry])
  }

  /**
   * Add a dataset to a user's profile in the meta-store
   *
   * @param uname username to check for
   * @param dataset dataset to add
   * @return unit
   */
  private def addUserDataset(uname: String, dataset: DataSetEntry) = {
    val filePath = Paths.get(s"./.datastore/meta/usersets/$uname.dat")
    val dsdir = Paths.get("./.datastore")
    val dmdir = Paths.get("./.datastore/meta")
    val dudir = Paths.get(s"./.datastore/meta/usersets")
    if (!Files.exists(dsdir)) Files.createDirectories(dsdir)
    if (!Files.exists(dmdir)) Files.createDirectories(dmdir)
    if (!Files.exists(dudir)) Files.createDirectories(dudir)
    if (!Files.exists(filePath)) Files.createFile(filePath)
    try {
      Files.write(filePath, DatastoreManager.makeEntryText(dataset).getBytes, StandardOpenOption.APPEND)
    } catch {
      case _: Throwable => println("This line is never executed")
    }
  }

  /**
   * Remove a datset from a user's profile in the meta-store
   *
   * @param username username to look for
   * @param dsName dataset to remove
   * @return unit
   */
  private def removeUserDataset(username: String, dsName: String) = {
    val filePath = Paths.get(s"./.datastore/meta/usersets/$username.dat")
    if (Files.exists(filePath)) {
      val stream = Source.fromFile(s"./.datastore/meta/usersets/$username.dat")
      val items = for (lines <- stream.getLines()) yield lines
      stream.close()
      items.filter(_ contains dsName)
      Files.write(filePath, items.mkString("\n").getBytes(), StandardOpenOption.TRUNCATE_EXISTING)
    }
  }

  /**
   * Modify the status of a dataset for a particular user
   * Status is required to check weather a dataset is available or not,
   * it can also be used to mark the datsets which are to be removed from the system
   *
   * @param username username to look for
   * @param data dataset to modify
   * @param newStatus new status of the dataset
   * @return unit
   */
  private def modifyStatus(username: String, data: DataSetEntry, newStatus: String) = {
    val filePath = Paths.get(s"./.datastore/meta/usersets/$username.dat")
    if (Files.exists(filePath)) {
      val stream = Source.fromFile(s"./.datastore/meta/usersets/$username.dat")
      val items = for (lines <- stream.getLines()) yield lines
      stream.close()
      val myEntry = DatastoreManager.makeEntryText(data)
      val newset = items.map { item =>
        if (item == myEntry) {
          data match {
            case DataSetEntry(name, desc, datatype, targetAlgo, status, source) => s"$name::$datatype::$targetAlgo::$newStatus::$source"
          }
        } else item
      }
      Files.write(filePath, newset.mkString("\n").getBytes(), StandardOpenOption.TRUNCATE_EXISTING)
    }
  }

  /**
   * Removes a user entirely
   * @param name name of the user
   */
  private def removeUserEntirely(name: String): Unit = {
    val filepath = Paths.get(s"./.datastore/meta/usersets/$name")
    if (Files.exists(filepath)) Files.delete(filepath)
  }

  /**
   * Check if a dataset is owned by a user
   * @param username
   * @param ds
   * @return Option[DatasetEntry]
   */
  private def checkUserDataset(username: String, ds: String) = {
    val stream = Source.fromFile(s"./.datastore/meta/usersets/$username.dat")
    val vals = stream.getLines().find(_ == ds)
    stream.close()
    vals match {
      case Some(x: String) => Some(DatastoreManager.makeDsEntry(x))
      case None => None
    }
  }

  def receive = {
    case GiveUserData(username: String) => sender ! getUserDatasets(username)
    case GiveRawUserData(username: String) => sender ! getRawUserDatasets(username)
    case AddDatasetRecord(username: String, data: DataSetEntry) => addUserDataset(username, data)
    case RemoveDatasetRecord(username: String, dsName: String) => removeUserDataset(username, dsName)
    case ModifyDatasetStatus(username: String, data: DataSetEntry, newStatus: String) => modifyStatus(username, data, newStatus)
    case RemoveUserEntirely(username: String) => removeUserEntirely(username)
    case CheckUserDataset(username: String, ds: String) => checkUserDataset(username, ds)
  }

}
