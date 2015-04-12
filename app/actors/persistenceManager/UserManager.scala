package actors.persistenceManager

import java.io.FileWriter

import akka.actor.{ActorSystem, TypedActor, TypedProps}
import models.batch._

import scala.concurrent.Future
import scala.io.Source

/**
 * Created by chetan on 12/04/15.
 * This modules checks for existing users and adds new user to the dataset
 */

object UserManager {

  private var singleton = true

  /**
   * Checks for existing users in the records
   * @param username
   * @return
   */
  def checkUsername(username: String) = {
    lazy val users = (for {
      line <- Source.fromFile("./datastore/users").getLines()
      record <- line.split("::").map(_ trim)
    } yield record(0)).toStream

    users contains username
  }

  def getRawUsersRec = {
    Source.fromFile("./datastore/users").getLines()
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

    if (UserManager.checkUsername(username)) {
      OperationStatus.OpFailure
    } else {

      try {
        lazy val fw = new FileWriter("./datastore/users")

        // Write off existing records before overwrite
        lazy val existing = UserManager.getRawUsersRec
        existing.map(fw.write(_))

        fw.write(s"$username::$password")
        fw.close()
        OperationStatus.OpSuccess
      } catch {
        case _ => OperationStatus.OpWarning
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
    lazy val existing = UserManager.getRawUsersRec

    lazy val newRecs = for {
      oldrecs <- Source.fromFile("./datastore/users").getLines()
      out <- if (oldrecs.split("::").map(_.trim)(0) == username)
        s"$username::$password"
      else oldrecs
    } yield out

    try {
      lazy val fw = new FileWriter("./datastore/users")

      newRecs.map(fw.write(_))
      fw.close()
      OperationStatus.OpSuccess
    } catch {
      case _ => OperationStatus.OpFailure
    }
  }

}
