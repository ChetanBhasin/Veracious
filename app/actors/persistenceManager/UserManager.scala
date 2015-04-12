package actors.persistenceManager

import java.io.FileWriter

import models.batch._

import scala.concurrent.Future
import scala.io.Source

/**
 * Created by chetan on 12/04/15.
 * This modules checks for existing users and adds new user to the dataset
 */

object UserManager {

  def checkUsername(username: String) = {
    lazy val users = (for {
      line <- Source.fromFile("./datastore/users").getLines()
      record <- line.split("::").map(_ trim)
    } yield record(0)).toStream

    users contains username
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

      val fw = new FileWriter("./datastore/users")

      try {
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
  def changePassword(username: String, password: String) =
    Future.successful(???)

}
