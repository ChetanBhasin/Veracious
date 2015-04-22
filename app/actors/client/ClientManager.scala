package actors.client

import actors.application.AppModule
import actors.mediator.RegisterForReceive
import akka.actor.ActorRef
import akka.pattern.ask
import models.messages.client._
import models.messages.logger.{GetLogs, Log}
import models.messages.persistenceManaging.DataSetEntry
import play.api.libs.json._

import scala.collection.mutable.{Map => mMap}
import scala.concurrent.Future
import scala.util.Success

/**
 * Created by basso on 22/04/15.
 */

object ClientManager {
}

import models.jsonWrites._

import scala.concurrent.duration._

class ClientManager (val mediator: ActorRef) extends AppModule {
  mediator ! RegisterForReceive(self, classOf[ClientMgrMessage])
  val clientTable = mMap[String, ActorRef]()
  import context.dispatcher       // Exectution context for Future
  implicit val timeout = 3 seconds

  def receive = {
    case LogIn(username) =>
      clientTable += ((username, sender))   // Invariant, the user is unique
      mediator ? GetLogs(username).asInstanceOf[Future[JsValue]].onComplete {
        case Success(logs) => clientTable(username) ! Push(Json.obj("logs" -> logs))
      }
      // TODO: Then the rest: Request for DataSet

    case LogOut(username) =>
      clientTable -= username

    case MessageToClient(user, pushData) =>
      clientTable get user match {
        case Some (act) => pushData match {
          case lg: Log => act ! Push(Json.obj("log" -> Json.toJson(lg)))
          case ds: DataSetEntry => Push(Json.obj("data-set" -> Json.toJson(ds)))      // TODO: How to update data-set deletions and all that
        }
        case None => moduleError("Message for ghost client")
      }

  }
}
