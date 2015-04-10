package actors.logger

import java.io.{BufferedWriter, FileWriter, PrintWriter}

import actors.mediator._
import akka.actor.{Actor, ActorRef}
import models.messages.client.{LogIn, LogOut, MessageToClient}
import models.messages.logger.Log
import play.api.libs.json._

import scala.collection.mutable.{ListBuffer, Map => mMap}
import scala.io.Source
/**
 * Created by basso on 09/04/15.
 *
 * The Logger system is responsible for all the logging activities related to the end-user.
 * It maintains a single text file which contains the log of all the user events.
 *
 */

import actors.logger.Logger._
class Logger (implicit val logFile: String, mediator: ActorRef) extends Actor {
  mediator ! RegisterForReceive(self, classOf[Log])
  val userList = ListBuffer[String]()

  def receive = {

    case LogIn (username) =>
      /** The username has just logged in, let us welcome him/her with logs **/
      userList += username
      sender ! MessageToClient (
        username,   // Bit redundant?
        Json.obj("log" -> getLogs(username))
      )

      /** The user has logged out, delete him/her from records */
    case LogOut (username) => userList -= username

      /** Got a log event, write the log to file and then notify the user if he/she is logged in */
    case lg @ Log(status, username, msg, event) =>
      val logObj = createAndWriteLog(lg)    // Create the log object and write to file as well
      if (userList.contains(username)) // If the user is logged in
        mediator ! MessageToClient (username, Json.obj("log" -> logObj))

  }
}


/**
 * Design decision: We will use Json file for logs
 *
 * A typical log will be:
 * {
 *   user: String,
 *   log: {
 *     status: String,
 *     message: String,
 *     activity: String
 *   }
 * }
 *
 */

object Logger {

  //val logFile = "./resources/operationLog"

  /**
   * This function will take a line of the file and return the log object if the corresponding
   * log belongs to the given user
   * @param user The username of the user we are searching the log file for
   * @param line A single line from the file
   * @return Option[log] where log is a JsValue
   */
  def filterUserLog (user: String) (line: String) = {
    val data = Json.parse(line)
    data \ "user" match {
      case JsString(`user`) => Some(data \ "log")
      case _ => None
    }
  }

  /**
   * Search the logFile for logs related to the user and return all of them in the final
   * Json array format
   * @param user the user we are interested in
   * @return JsArray[logs]
   */
  def getLogs (user: String) (implicit logFile: String): JsArray = {
    val stream = Source.fromFile(logFile)
    JsArray((
      for {
        line <- stream.getLines()
        log <- filterUserLog(user)(line)
      } yield log
      ).toSeq
    )
  }

  def createAndWriteLog (logEvent: Log) (implicit logFile: String): JsValue = {
    // We first create the log object
    val logObj = Json.obj(
      "status" -> logEvent.status.toString.substring(2).toUpperCase,
      "message" -> logEvent.msg,
      "activity" -> logEvent.content.logWrite
    )
    // Write the object to file immediately
    val writer = new PrintWriter (new BufferedWriter (new FileWriter (logFile, true)))
    writer.println(
      Json.stringify(
        Json.obj(
          "user" -> logEvent.user,
          "log" -> logObj
        )
      )
    )
    writer.close()
    // return the log object
    logObj
  }
}
