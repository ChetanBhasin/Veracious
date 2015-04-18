package actors.miner

import akka.actor._
import models.batch.job.{MnALS, MnClustering, MnFPgrowth, MnSVM}
import models.mining.mlops._

/**
 * Created by basso on 14/04/15.
 * TODO: Chetan has to finish this one
 */
class Worker (mediator: ActorRef) extends Actor {

  private def handleALS(als: MnALS) = {
    val sparkWorker = als match {
      case MnALS(train: String, query: String, ranks: Int, maxIter: Int, id: String) => new VALS(train, ranks, maxIter)
    }
  }

  private def handleSVM(svm: MnSVM) = {
    val sparkWorker = svm match {
      case MnSVM(trin: String, test: String, maxIter: Int, id: String) => new VSVM(trin, test, maxIter)
    }
  }

  private def handleFPM(fpm: MnFPgrowth) = {
    val sparkWorker = fpm match {
      case MnFPgrowth(name: String, minSupport: Double, id: String) => new VFPM(name, minSupport)
    }
  }

  private def handleClusering(clustering: MnClustering) = {
    val sparkWorker = clustering match {
      case MnClustering(name: String, test: Option[String], maxIter: Int, clusters: Int, id: String) => new VClustering(name, test, clusters, maxIter)
    }
  }

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

    case als: MnALS => handleALS(als)
    case svm: MnSVM => handleSVM(svm)
    case fpm: MnFPgrowth => handleFPM(fpm)
    case clustering: MnClustering => handleClusering(clustering)
  }

}
