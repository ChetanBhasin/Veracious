package actors.persistenceManager

import java.io.{BufferedWriter, FileWriter, PrintWriter}
import java.nio.file.{Files, Paths}

import akka.actor.{ActorRef, ActorSystem, TypedActor, TypedProps}
import models.batch.OperationStatus.OperationStatus
import models.batch._

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
object UserManagerImpl {

  // Check for singleton since only one actor of such kind should exist
  private var singleton = true

  /**
   * Make sure that the environment on the disk is friendly for use.
   * If not, create files and directories for data and meta store.
   * @return
   */
  private[persistenceManager] def checkSources = {
    lazy val PathStoreDir = Paths.get("./.datastore/")
    lazy val PathStoreMetaDir = Paths.get("./.datastore/meta/")
    lazy val PathStoreUsers = Paths.get("./.datastore/meta/users.dat")

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

    lazy val stream = Source.fromFile("./.datastore/meta/users.dat")

    stream.getLines.map(_.split("::")(0)) contains username
  }

  /**
   * Get raw data regarding available users on the system
   * @return
   */
  def getRawUsersRec: Array[String] = {
    lazy val stream = Source.fromFile("./.datastore/meta/users.dat")
    lazy val lines = stream.getLines.toArray //finally (stream.close)
    lines
  }

  /**
   * Returns a typed actor of type User Manager
   * @param system The ActorSystem to be used
   * @return
   */
  def apply(system: ActorSystem, mediator: ActorRef) = {
    if (singleton) {
      singleton = false
      val obj: UserManager = TypedActor(system).typedActorOf(TypedProps(classOf[UserManagerImpl], new UserManagerImpl(mediator)))
      obj
    } else {
      throw new Exception("Only one object at a time is allowed")
    }
  }

}

trait UserManager {
  def checkUsername(username: String): Boolean

  def addUser(username: String, password: String): OperationStatus

  def removeUser(username: String): OperationStatus

  def changePassword(username: String, password: String): OperationStatus

  def authenticate(username: String, password: String): Boolean
}

/**
 * Class intented to be used as TypedActor for user management
 */
private[persistenceManager] class UserManagerImpl(mediator: ActorRef) extends UserManager {

  /**
   * Authenticate a user
   * @param username username of the user
   * @param password expected password of the user
   * @return Boolean
   */
  def authenticate(username: String, password: String) = try {
    val user = UserManagerImpl.getRawUsersRec.filter(_ contains username)(0).split("::")
    user(1) == password
  } catch {
    case _: Throwable => false
  }


  /**
   * Check if a particular user exists
   * @param username
   * @return
   */
  def checkUsername(username: String) = UserManagerImpl.checkUsername(username)

  /**
   * Add a user if it doesn't already exists
   * @param username
   * @param password
   * @return
   */
  def addUser(username: String, password: String) = {

    val PathStoreFileUser = UserManagerImpl.checkSources

    if (UserManagerImpl.checkUsername(username)) {
      OperationStatus.OpFailure
    } else {
      try {
        val writer = new PrintWriter(new BufferedWriter(new FileWriter("./.datastore/meta/users.dat", true)))
        writer.println(s"$username::$password")
        writer.close
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
  def removeUser(username: String) = {
    UserManagerImpl.checkSources

    val stream = Source.fromFile("./.datastore/meta/users.dat")

    val newRecs = try {
      stream.getLines.filter(_ contains username)
    } finally {
      stream.close
    }

    try {
      val writer = new PrintWriter(new BufferedWriter(new FileWriter("./.datastore/meta/users.dat", false)))
      newRecs.map(writer.println(_))
      writer.close()
      mediator ! RemoveUserEntirely(username)
      OperationStatus.OpSuccess
    } catch {
      case _: Throwable => OperationStatus.OpFailure
    }
  }

  /**
   * Change the password for a username
   * @param username
   * @param password
   * @return
   */
  def changePassword(username: String, password: String) = {

    UserManagerImpl.checkSources

    val stream = Source.fromFile("./.datastore/meta/users.dat")

    val newRecs = try {
      stream.getLines.filter(_ contains username)
    } finally {
      stream.close
    }

    try {
      val writer = new PrintWriter(new BufferedWriter(new FileWriter("./.datastore/meta/users.dat", false)))
      newRecs.map(writer.println(_))
      writer.println(s"$username::$password")
      writer.close()
      OperationStatus.OpSuccess
    } catch {
      case _: Throwable => OperationStatus.OpFailure
    }
  }

}