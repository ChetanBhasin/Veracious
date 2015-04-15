package actors.persistenceManager

import akka.actor.{Actor, ActorRef, Props, Terminated}
import akka.routing.{ActorRefRoutee, Router, SmallestMailboxRoutingLogic}
import models.messages.Ready
import models.messages.batchProcessing.SubmitDsOpJob
import models.messages.persistenceManaging._

/**
 * Created by chetan on 12/04/15.
 */


/**
 * Persistence actor
 * Role: Communicate directly with the disk and perform read/write operations.
 */
class Persistence(mediator: ActorRef) extends Actor {

  // UserManager TypedActor for user related meta operations
  lazy val userManager = UserManager(context system)
  // DatastoreManager TypedActor for datastore related meta operations
  lazy val datastoreManager = DatastoreManager(context system)

  override def preStart(): Unit = {
    context.parent ! Ready("Persistence")
  }

  // Router to route jobs to Worker actors
  var router = {
    val routee = Vector.fill(getChildActors) {
      val r = context actorOf Props(classOf[WorkerActor], mediator)
      context watch r
      ActorRefRoutee(r)
    }
    Router(SmallestMailboxRoutingLogic(), routee)
  }


  def receive = {
    // Request for UserManager actor
    case getUserManager => sender ! userManager
    case getDatastoreManager => sender ! datastoreManager

    case ListUserData(username: String) => datastoreManager !(GiveUserData(username), sender)

    case operation: SubmitDsOpJob => router.route((operation, datastoreManager), sender())

    case Terminated(routee) => {
      router = router.removeRoutee(routee)
      val r = context actorOf (Props[WorkerActor])
      context watch r
      router.addRoutee(r)
    }
  }

}
