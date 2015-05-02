package actors.logger

import java.io.{BufferedWriter, FileWriter, PrintWriter}

import actors.application.AppModule
import actors.mediator._
import akka.actor.ActorRef
import models.messages.client.MessageToClient
import models.messages.logger.{GetLogs, Log, LoggerMessage}
import play.api.libs.json._

import scala.collection.mutable.{Map => mMap}
import scala.io.Source
import scala.util.{Failure, Success, Try}

/**
 * Created by basso on 09/04/15.
 *
 * The Logger system is responsible for all the logging activities related to the end-user.
 * It maintains a single text file which contains the log of all the user events.
 *
 */

/** BIG NOTE ** implicit parameters always to the right,
  *  Casued me Super headache
  */
import actors.logger.Logger._
class Logger (val mediator: ActorRef, implicit val logFile: String) extends AppModule {
  mediator ! RegisterForReceive(self, classOf[LoggerMessage])

  def this(mediator: ActorRef) =
    this(mediator, Logger.productionLogFile)

  def receive = {
      /** Got a log event, write the log to file and then notify the user if he/she is logged in */
    case lg @ Log(status, username, msg, event) =>
      writeLog(lg) match {
        case Success(_) => mediator ! MessageToClient(username, lg)
        case Failure(ex) => moduleError("Could'nt write log to disk: Exception => "+ex)
      }

    case GetLogs (username) => sender ! getLogs(username)   // Send back the array as it is
  }
}

object Logger {

  private val productionLogFile = "./resources/operationLog"

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

  import models.jsonWrites._
  def writeLog (logObject: Log) (implicit logFile: String): Try[Unit] = Try {
    val writer = new PrintWriter (new BufferedWriter (new FileWriter (logFile, true)))
    writer.println(
      Json.stringify(
        Json.obj(
          "user" -> logObject.user,
          "log" -> Json.toJson(logObject)
        )
      )
    )
    writer.close()
  }
}
