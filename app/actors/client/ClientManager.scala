package actors.client

import actors.application.AppModule
import actors.mediator.RegisterForReceive
import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import models.messages.batchProcessing.BatchProcessorMessage
import models.messages.client._
import models.messages.logger.{GetLogs, Log}
import models.messages.persistenceManaging.{DataSetEntry, GetUserDataSets, GetUserResults}
import play.api.libs.json._

import scala.collection.mutable.{Map => mMap}
import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
 * Created by basso on 22/04/15.
 */

object ClientManager {
}

import models.jsonWrites._

import scala.concurrent.duration._

class ClientManager (val mediator: ActorRef) extends AppModule {
  mediator ! RegisterForReceive(self, classOf[ClientManagerMessage])
  val clientTable = mMap[String, ActorRef]()
  import context.dispatcher       // Exectution context for Future
  implicit val timeout = Timeout(30 seconds)

  def receive = {
    case LogIn(username) =>
      clientTable += ((username, sender))   // Invariant, the user is unique
      mediator ! new LogIn(username) with BatchProcessorMessage     // Notifiy the batch processor that a new user has logged in
      (mediator ? GetLogs(username)).asInstanceOf[Future[JsValue]].onComplete {
        case Success(logs) => clientTable(username) ! Push(Json.obj("logs" -> logs))
        case Failure(ex) => moduleError("Couldn't get logs: exception =>"+ex)
      }
      (mediator ? GetUserDataSets(username)).asInstanceOf[Future[JsValue]].onComplete {
        case Success(ds) => clientTable(username) ! Push(Json.obj("data-sets" -> ds))
        case Failure(ex) => moduleError("Couldn't get Data-sets: exception =>"+ex)
      }
      (mediator ? GetUserResults(username)).asInstanceOf[Future[JsValue]].onComplete {
        case Success(ds) => clientTable(username) ! Push(Json.obj("results" -> ds))
        case Failure(ex) => moduleError("Couldn't get results: exception =>"+ex)
      }

    case LogOut(username) =>
      clientTable -= username
      mediator ! new LogOut(username) with BatchProcessorMessage

    case MessageToClient(user, pushData) =>
      clientTable get user match {
        case Some (act) => pushData match {
          case lg: Log => act ! Push(Json.obj("log" -> Json.toJson(lg)))
          case ds: DataSetEntry => Push(Json.obj("data-set" -> Json.toJson(ds)))      // TODO: How to update data-set deletions and all that
        }
        case None => Unit   // The client is unavailable, so no push
      }

  }
}
