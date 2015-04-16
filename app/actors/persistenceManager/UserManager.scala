package actors.persistenceManager

import java.io.FileWriter
import java.nio.file.{Files, Paths, StandardOpenOption}

import akka.actor.{ActorSystem, TypedActor, TypedProps}
import models.batch.OperationStatus.OperationStatus
import models.batch._

import scala.concurrent.Future
import scala.io.Source

/**
 * Created by chetan on 12/04/15.
 * This modules checks for existing users and adds new user to the dataset
 */

/**
 * Companion object for class UserManager
 *
 * Responsibility: To act as the TypedActor which should
 * keep track of all the user records.
 * It should be able to add/remove/get details of/update details of all the available users
 * on the system.
 */
private[persistenceManager] object UserManagerImpl {

  // Check for singleton since only one actor of such kind should exist
  private var singleton = true

  /**
   * Make sure that the environment on the disk is friendly for use.
   * If not, create files and directories for data and meta store.
   * @return
   */
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

    // Check sources before proceeding
    this.checkSources

    lazy val stream = Source.fromFile("./datastore/meta/users.dat")

    lazy val users = (for {
      line <- stream.getLines
      record <- line.split("::").map(_ trim)
    } yield record(0)).toStream

    stream.close

    users contains username
  }

  /**
   * Get raw data regarding available users on the system
   * @return
   */
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
      val obj: UserManager = TypedActor(system).typedActorOf(TypedProps[UserManagerImpl]())
      obj
    } else {
      throw new Exception("Only one object at a time is allowed")
    }
  }

}

trait UserManager {
  def checkUsername(username: String): Future[Boolean]

  def addUser(username: String, password: String): OperationStatus

  def removeUser(username: String): OperationStatus

  def changePassword(username: String, password: String): OperationStatus
}

/**
 * Class intented to be used as TypedActor for user management
 */
private[persistenceManager] class UserManagerImpl extends UserManager {


  /**
   * Check if a particular user exists
   * @param username
   * @return
   */
  def checkUsername(username: String) = Future.successful(UserManagerImpl.checkUsername(username))

  /**
   * Add a user if it doesn't already exists
   * @param username
   * @param password
   * @return
   */
  def addUser(username: String, password: String) = {

    lazy val PathStoreFileUser = UserManagerImpl.checkSources

    if (UserManagerImpl.checkUsername(username)) {
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
  def removeUser(username: String) = ???

  /**
   * Change the password for a username
   * @param username
   * @param password
   * @return
   */
  def changePassword(username: String, password: String) = {

    UserManagerImpl.checkSources

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
