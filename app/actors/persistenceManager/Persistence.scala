package actors.persistenceManager

import actors.application.AppModule
import actors.mediator.RegisterForReceive
import akka.actor.{ActorRef, Props, Terminated}
import akka.routing.{ActorRefRoutee, Router, SmallestMailboxRoutingLogic}
import models.messages.batchProcessing.{DsOperatorMessage, SubmitDsOpJob}
import models.messages.persistenceManaging._
import models.mining.MinerResult

/**
 * Created by chetan on 12/04/15.
 */

case class GetUserDatasetsJson(username: String)

/**
 * Persistence actor
 * Role: Communicate directly with the disk and perform read/write operations.
 */
//TODO:: All messed up this one is
class Persistence(val mediator: ActorRef) extends AppModule {

  mediator ! RegisterForReceive (self, classOf[PersistenceMessage])
  mediator ! RegisterForReceive (self, classOf[DsOperatorMessage])

  // UserManager TypedActor for user related meta operations
  lazy val userManager = UserManagerImpl(context system)
  // DatastoreManager TypedActor for datastore related meta operations
  lazy val datastoreManager = DatastoreManager(context system)

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
    case GetUserManager => sender ! userManager
    case GetDataStoreManager => sender ! datastoreManager

    case ListUserData(username: String) => datastoreManager !(GiveUserData(username), sender)

    case operation: SubmitDsOpJob => router.route((operation, datastoreManager), sender)

    case GetUserDatasetsJson(username: String) => router.route((GetUserDatasetsJson(username), datastoreManager), sender)

    case operation: MinerResult => router.route((operation, datastoreManager), sender)

    case Terminated(routee) => {
      router = router.removeRoutee(routee)
      val r = context actorOf (Props[WorkerActor])
      context watch r
      router.addRoutee(r)
    }
  }

}
