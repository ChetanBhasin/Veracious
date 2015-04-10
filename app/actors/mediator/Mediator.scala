package actors.mediator

import akka.actor.{Actor, ActorRef, Terminated}

/**
 * Created by basso on 13/4/14.
 * The Mediator actor implements the mediator pattern
 * It can become a central point of communication between many actors
 */

class Mediator extends Actor {
  import collection.mutable.{Map => mMap, Set => mSet}

    // For personal messages and broadcast, message type -> list of colleagues interested
  val fTable = mMap[Class[_], mSet[ActorRef]]()

  val notifySet = mSet[ActorRef]()    // For broadcast only
  val globalMsg = mSet[Class[_]]()    // Types for broadcast

  def sendMsgFn(sender: ActorRef)(act: ActorRef, msg: Any) {
    if (act != sender) act ! msg
  }

  def receive = {
      // Register a message type for broadcast to all colleagues
    case RegisterBroadcastMessage(msg) => globalMsg += msg

      // Registering a colleague for broadcast messages only
    case RegisterForNotification(act) =>
      notifySet += act
      context watch act

      // Register a colleague for personal and broadcast messages
    case RegisterForReceive(act, mt) =>
      fTable += ((mt, fTable.getOrElse(mt, mSet[ActorRef]())+act ))
      context watch act

      // un-register a colleague from the mediator
    case Unregister(act) =>
      context unwatch act
      fTable.foreach { case(m, al) =>
        al -= act
      }
      notifySet -= act

      // in-case actor terminates, un-register
    case Terminated(act) => self ! Unregister(act)

      // Actual Message forwarding algorithm
    case msg =>
      val sendMsg = sendMsgFn(sender)_
      if (globalMsg.exists{ _.isAssignableFrom(msg.getClass)}) {    // First check if its a broadcast
        notifySet.foreach{sendMsg(_, msg)}
        fTable.values.reduce(_++_).foreach{sendMsg(_, msg)}
      }
      else        // Its a personal message
        fTable.foreach { case(m, al) =>           // a message is sent to anyone who has registered
          if (m.isAssignableFrom(msg.getClass))   // that type or a super-type
            al.foreach{sendMsg(_, msg)}
        }
  }
}
