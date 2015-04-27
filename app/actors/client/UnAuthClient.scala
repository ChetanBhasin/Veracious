package actors.client

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import play.api.libs.json._

/**
 * Created by basso on 27/04/15.
 * When an unauthorised client tries to connect
 * TODO: There is loophole in the authorisation scheme, check controllers.Application
 */
class UnAuthClient(out: ActorRef) extends Actor with ActorLogging {
  override def preStart() {
    out ! Json.obj(
      "error" -> "You are unauthorised to connect"
    )
    context stop self
  }
  def receive = {
    case msg => log.warning("Unauth client received message: "+ msg)
  }
}

object UnAuthClient {
  def props(out: ActorRef) = Props(classOf[UnAuthClient], out)
}
