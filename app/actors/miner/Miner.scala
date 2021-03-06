package actors.miner

import actors.application.AppModule
import actors.mediator.RegisterForReceive
import akka.actor._
import akka.routing._
import models.messages.batchProcessing._

/**
 * Entry point for the Miner subsystem
 */
class Miner(val mediator: ActorRef) extends AppModule {

  mediator ! RegisterForReceive(self, classOf[MinerMessage])

  val router = Router(
    SmallestMailboxRoutingLogic(), // Have to decide
    Vector.fill(getWorkerCount)(ActorRefRoutee(
      context.actorOf(Props(classOf[Worker], mediator))
    ))
  )

  def receive = {
    case work: SubmitMineJob =>
      println("Got a mine job: "+work.job)
      router.route(work, sender)
  }

}

object Miner {


}
