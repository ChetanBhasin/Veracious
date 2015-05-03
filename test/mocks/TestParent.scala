package mocks

import akka.actor._

/**
 * Created by basso on 12/04/15.
 */
class TestParent (childProps: Props, parProbe: ActorRef)
  extends Actor {

  val child = context.actorOf(childProps, "testChild")

  def receive = {
    case msg if sender == child => parProbe ! msg
    case msg => child.tell(msg, sender)
  }

}
