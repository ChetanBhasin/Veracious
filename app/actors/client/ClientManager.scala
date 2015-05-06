package actors.client

import actors.application.AppModule
import actors.mediator.RegisterForReceive
import actors.persistenceManager.GetDsData
import akka.actor.{ActorRef, PoisonPill}
import akka.pattern.ask
import akka.util.Timeout
import models.batch.OperationStatus
import models.messages.application.{FinishWork, FinishedWork}
import models.messages.batchProcessing.BatchProcessorMessage
import models.messages.client._
import models.messages.logger.{GetLogs, Log}
import models.messages.persistenceManaging.{DataSetEntry, GetUserDataSets}
import play.api.libs.json._

import scala.collection.mutable.{Map => mMap}
import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
 * Created by basso on 22/04/15.
 */

object ClientManager {
  case class PushData(username: String, data: JsValue)
}
import actors.client.ClientManager._
import models.jsonWrites._

import scala.concurrent.duration._

class ClientManager (val mediator: ActorRef) extends AppModule {
  mediator ! RegisterForReceive(self, classOf[ClientManagerMessage])
  val clientTable = mMap[String, ActorRef]()
  import context.dispatcher       // Exectution context for Future
  implicit val timeout = Timeout(30 seconds)

  def updateClientData(username: String) =
    (mediator ? GetUserDataSets(username)).asInstanceOf[Future[JsValue]].onComplete {
      case Success(ds) =>
        println("Just pushing datasets")
        self ! PushData(username, Json.obj("datasets" -> ds))
      case Failure(ex) => moduleError("Couldn't get Data-sets: exception =>"+ex)
    }

  def receive = {
    case UserAlreadyLoggedIn(username) =>
      sender ! clientTable.contains(username)

    case LogIn(username) =>
      clientTable += ((username, sender))   // Invariant, the user is unique
      mediator ! new LogIn(username) with BatchProcessorMessage     // Notifiy the batch processor that a new user has logged in
      (mediator ? GetLogs(username)).asInstanceOf[Future[JsValue]].onComplete {
        case Success(logs) => self ! PushData(username, Json.obj("logs" -> logs))
        case Failure(ex) => moduleError("Couldn't get logs: exception =>"+ex)
      }
      updateClientData(username)

    case LogOut(username) =>
      clientTable -= username
      mediator ! new LogOut(username) with BatchProcessorMessage

    case MessageToClient(user, pushData) =>
      clientTable get user match {
        case Some (act) => pushData match {
          case lg: Log =>
            act ! Push(Json.obj("log" -> Json.toJson(lg)))
            if (lg.status == OperationStatus.OpSuccess) {
              println("Client manager about to call update")
              updateClientData(user)
            }
          case ds: DataSetEntry => Push(Json.obj("data-set" -> Json.toJson(ds))) // NOT needed actually
        }
        case None => Unit   // The client is unavailable, so no push
      }

    case FinishWork =>
      clientTable.values.foreach { _ ! PoisonPill }
      sender ! FinishedWork

    case PushData(user, obj) =>
      println("pushing::: "+obj)
      clientTable get user match {
        case Some (act) => act ! Push(obj)
        case None => Unit
      }

    case AskForResult(user, dsName) =>
      mediator ? GetDsData(user, dsName) onComplete {
        case Failure(_) => self ! PushData(user, Json.obj("resultError" -> "Could Not retrieve result"))
        case Success(res: JsValue) => self ! PushData(user, Json.obj("result" -> res))
      }
  }
}
