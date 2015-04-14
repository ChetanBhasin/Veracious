package actors.dsOperator
import akka.actor._

/**
 * Created by basso on 14/04/15.
 */
class Worker (mediator: ActorRef) extends Actor {
  def receive = {
    /** Do action against every SubmitDsOp work received
      * When done, send a Log object to the mediator
      * and send a JobStatus back to sender
      */
  }
}
