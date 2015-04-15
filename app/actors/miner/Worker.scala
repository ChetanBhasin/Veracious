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
      *
      * 1. Mine
      * 2. Send Result to Persistence,
      * 3. Persistence will reply with write status
      * 4. Once all of this is finished, send Log(..) message to mediator
      * 4. And then send JobStatus back to sender
      */
    case _ => Unit
  }

}
