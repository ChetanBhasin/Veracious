package actors.persistenceManager

import akka.actor.Actor
import models.batch.job._
import models.messages.batchProcessing.SubmitDsOpJob


/**
 * Created by chetan on 13/04/15.
 */
class WorkerActor extends Actor {

  private def handleDsJob(username: String, job: DataSetOp) = job match {

    case DsAddFromUrl(name, desc, target_algo, url, id) => ???
    case DsAddDirect(name, desc, target_algo, file, id) => ???
    case DsDelete(name, id) => ???
    case DsRefresh(name, id) => ???

    case _ => println("Unhandled message")
  }

  def receive = {
    case SubmitDsOpJob(username: String, job: DataSetOp) => handleDsJob(username, job)
  }

}
