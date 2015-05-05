package actors.persistenceManager

import actors.application.AppModule
import actors.mediator.RegisterForReceive
import akka.actor.{ActorRef, Props, Terminated}
import akka.routing.{ActorRefRoutee, Router, SmallestMailboxRoutingLogic}
import models.messages.application.Ready
import models.messages.batchProcessing.{DsOperatorMessage, SubmitDsOpJob}
import models.messages.persistenceManaging._
import models.mining.MinerResult

/**
 * Created by chetan on 12/04/15.
 */

case class GetUserDatasetsJson(username: String) extends PersistenceMessage

// This class will not extends PersistenceMessage yet... will be mixed in later
case class GetDsData(username: String, Ds: String) extends PersistenceMessage

/**
 * Persistence actor
 * Role: Communicate directly with the disk and perform read/write operations.
 */
//TODO:: All messed up this one is
class Persistence(val mediator: ActorRef) extends AppModule {

  mediator ! RegisterForReceive (self, classOf[PersistenceMessage])
  mediator ! RegisterForReceive (self, classOf[DsOperatorMessage])
  mediator ! RegisterForReceive(self, classOf[RemoveUserEntirely])

  // UserManager TypedActor for user related meta operations
  val userManager = UserManagerImpl(context system, context self)
  // DatastoreManager TypedActor for datastore related meta operations
  val datastoreManager = DatastoreManager(context system)

  override def preStart() {
    context.parent ! Ready(this.getClass)
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

    /**
     * Get userManager ActorRef
     */
    case GetUserManager => sender ! userManager

    /**
     * Get datastoreManager ActorRef
     */
    case GetDataStoreManager => sender ! datastoreManager

    /**
     * Get a user owned datasets' meta records in private message formate
     */
    case GetUserDataSets(username: String) => datastoreManager ! (GiveUserData(username), sender)

    /**
     * Submit a DsOpJob
     */
    case operation: SubmitDsOpJob => router.route((operation, datastoreManager), sender)

    /**
     * Perform a miner result operation
     */
    case operation: MinerResult => router.route((operation, datastoreManager), sender)

    /**
     * Remove the user entirely
     * This call is used to pass the message to DSM
     */
    case RemoveUserEntirely(username) => datastoreManager ! RemoveUserEntirely(username)

    /**
     * Return a dataset's data to the user
     */
    case operation: GetDsData => router.route((operation, datastoreManager), sender)

    /**
     * Renew and reroute expired children actor
     */
    case Terminated(routee) => {
      router = router.removeRoutee(routee)
      val r = context actorOf (Props[WorkerActor])
      context watch r
      router.addRoutee(r)
    }
  }

}
