package actors.dsOperator

import actors.mediator._
import akka.actor._
import akka.routing._
import models.messages.batchProcessing._

/**
 * Created by basso on 14/04/15.
 */
class DsOperator (mediator: ActorRef) extends Actor {
  mediator ! RegisterForReceive (self, classOf[DsOperatorMessage])

  val router = Router(
    SmallestMailboxRoutingLogic (),
    Vector.fill (DsOperator.getWorkerCount) ( ActorRefRoutee (
      context.actorOf(Props(classOf[Worker], mediator))
    ))
  )

  // TODO: Implement the data-set listing and partial meta-data

  def receive = {
    case work: SubmitDsOpJob => router.route(work, sender)
  }

}

object DsOperator {
    // TODO
  def getWorkerCount: Int = 5
}
