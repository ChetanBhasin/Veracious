package actors.persistenceManager

import java.io.FileWriter
import java.nio.file.{Files, Paths, StandardOpenOption}

import akka.actor.{ActorSystem, TypedActor, TypedProps}
import models.batch._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.Source

/**
 * Created by chetan on 12/04/15.
 * This modules checks for existing users and adds new user to the dataset
 */

object UserManager {

  private var singleton = true

  private[persistenceManager] def checkSources = {
    lazy val PathStoreDir = Paths.get("./datastore/")
    lazy val PathStoreMetaDir = Paths.get("./datastore/meta/")
    lazy val PathStoreUsers = Paths.get("./datastore/meta/users.dat")

    if (!Files.exists(PathStoreDir)) Files.createDirectory(PathStoreDir)
    if (!Files.exists(PathStoreMetaDir)) Files.createDirectory(PathStoreMetaDir)
    if (!Files.exists(PathStoreUsers)) Files.createFile(PathStoreUsers)

    PathStoreUsers
  }

  /**
   * Checks for existing users in the records
   * @param username
   * @return
   */
  def checkUsername(username: String) = {

    this.checkSources

    lazy val stream = Source.fromFile("./datastore/meta/users.dat")

    lazy val users = (for {
      line <- stream.getLines
      record <- line.split("::").map(_ trim)
    } yield record(0)).toStream

    stream.close

    users contains username
  }

  def getRawUsersRec = {
    lazy val stream = Source.fromFile("./datastore/meta/users.dat")
    lazy val lines = stream.getLines
    stream.close
    lines
  }

  /**
   * Returns a typed actor of type User Manager
   * @param system The ActorSystem to be used
   * @return
   */
  def apply(system: ActorSystem) = {
    if (singleton) {
      singleton = false
      TypedActor(system).typedActorOf(TypedProps[UserManager]())
    } else {
      throw new Exception("Only one object at a time is allowed")
    }
  }

}

/**
 * Class intented to be used as TypedActor for user management
 */
class UserManager {

  /**
   * Check if a particular user exists
   * @param username
   * @return
   */
  def checkUsername(username: String) = Future.successful(UserManager.checkUsername(username))

  /**
   * Add a user if it doesn't already exists
   * @param username
   * @param password
   * @return
   */
  def addUser(username: String, password: String) = Future {

    lazy val PathStoreFileUser = UserManager.checkSources

    if (UserManager.checkUsername(username)) {
      OperationStatus.OpFailure
    } else {
      try {
        Files.write(PathStoreFileUser, s"$username::$password".getBytes, StandardOpenOption.APPEND)
        OperationStatus.OpSuccess
      } catch {
        case _: Throwable => OperationStatus.OpWarning
      }
    }
  }

  /**
   * Remove a user from the database and all the assosiated databases
   * @param username
   * @return
   */
  def removeUser(username: String) = Future.successful(???)

  /**
   * Change the password for a username
   * @param username
   * @param password
   * @return
   */
  def changePassword(username: String, password: String) = Future {

    UserManager.checkSources

    lazy val stream = Source.fromFile("./datastore/meta/users.dat")
    lazy val newRecs = for {
      oldrecs <- stream.getLines
      out <- if (oldrecs contains username)
        s"$username::$password"
      else oldrecs
    } yield out
    stream.close

    try {
      lazy val fw = new FileWriter("./datastore/meta/users.dat")

      newRecs.map(fw.write(_))
      fw.close()
      OperationStatus.OpSuccess
    } catch {
      case _: Throwable => OperationStatus.OpFailure
    }
  }

}
