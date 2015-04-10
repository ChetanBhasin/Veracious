package actors.mocks

import actors.batchProcessor
import akka.actor._

                                    // the mediator probably will be a probe
class MockWorkerParent (user: String, mediator: ActorRef, probe: ActorRef)
  extends Actor{

  val child = context.actorOf(Props(classOf[batchProcessor.Worker], user, mediator), "testWorker")

  def receive = {
    case msg if sender == child => probe ! msg
    case msg => child ! msg
  }
}
