package actors.client

/**
 * Created by basso on 09/04/15.
 */

import actors.mediator._
import akka.actor._
import models.messages.client._

/**
 * This is the actor which will be created for each Client just to handle the web socket
 * @param username The unique username of the client
 * @param socket The web-socket handler actor (managed by the Action - WebSocket.acceptWithActor[JsValue, JsValue])
 * @param mediator The ActorRef of the Mediator
 */
class Client (val username: String, socket: ActorRef, mediator: ActorRef) extends Actor {
  mediator ! RegisterForReceive(self, classOf[MessageToClient])

  override def preStart() {
    mediator ! LogIn(username)
  }

  def receive = {
    // Match the message to our username
    case MessageToClient(`username`, msg) => socket ! msg
    case _ => Unit    // Ignore all others

    /** As of now, no messages to come from the browser using the socket. */
  }

  override def postStop() {   // Will run when this actor is killed. (The client logged out)
    mediator ! LogOut(username)
  }
}

object Client {
  // The required props for the client
  def props(mediator: ActorRef)(username: String, out: ActorRef) = Props(classOf[Client], username, out, mediator)
}

/** Reception API for the client web-socket
  1. Logs =>
    { log: [ {status, message, activity} ] }

*/