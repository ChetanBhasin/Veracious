package actors.miner

import akka.actor._

/**
 * Created by basso on 14/04/15.
 * TODO: Chetan has to finish this one
 */
class Worker (mediator: ActorRef) extends Actor {

  def receive = {
    /** Do action against every kind of SubmitMineJob here.
      * When finished, send a Log object to the mediator
      * and send a JobStatus back to the sender
      */
  }

}
