package actors.application

/**
 * Created by basso on 16/04/15.
 */

import actors.mediator.Unregister
import akka.actor.{Actor, ActorRef}
import models.messages.{Ready, SysError}

/**
 * This is the template for the major sub-systems of the application
 */
abstract class AppModule extends Actor {

  val mediator: ActorRef
  override def preStart() {
    context.parent ! Ready(this.getClass)
  }

  override def postStop() {
    mediator ! Unregister(self)
  }

  def moduleError (msg: String) = {
    context.parent ! SysError(this.getClass.getSimpleName, msg)
  }
}
