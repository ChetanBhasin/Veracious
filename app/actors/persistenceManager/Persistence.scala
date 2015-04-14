package actors.persistenceManager

import akka.actor.{Actor, Props, Terminated}
import akka.routing.{ActorRefRoutee, Router, SmallestMailboxRoutingLogic}
import akka.pattern.ask
import models.batch.job.DataSetOp
import models.messages.persistenceManaging._

/**
 * Created by chetan on 12/04/15.
 */


/**
 * Persistence actor
 * Role: Communicate directly with the disk and perform read/write operations.
 */
class Persistence extends Actor {

  // UserManager TypedActor for user related meta operations
  lazy val userManager = UserManager(context system)
  // DatastoreManager TypedActor for datastore related meta operations
  lazy val datastoreManager = DatastoreManager(context system)

  // Router to route jobs to Worker actors
  var router = {
    val routee = Vector.fill(getChildActors) {
      val r = context actorOf (Props[WorkerActor])
      context watch r
      ActorRefRoutee(r)
    }
    Router(SmallestMailboxRoutingLogic(), routee)
  }


  def receive = {
    // Request for UserManager actor
    case getUserManager => sender ! userManager
    case getDatastoreManager => sender ! datastoreManager

    case ListUserData(username: String) => sender ! (datastoreManager ? GiveUserData(username))

    case operation: DataSetOp => router.route(operation, sender())

    case Terminated(routee) => {
      router = router.removeRoutee(routee)
      val r = context actorOf (Props[WorkerActor])
      context watch r
      router.addRoutee(r)
    }
  }

}
