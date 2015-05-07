package actors.miner

import akka.actor._
import models.batch.job.{MnALS, MnClustering, MnFPM, MnSVM}
import models.messages.batchProcessing.SubmitMineJob
import models.mining
import models.mining.MinerResult
import models.mining.mlops._

/**
 * Created by basso on 14/04/15.
 * TODO: Chetan has to finish this one
 */
class Worker(mediator: ActorRef) extends Actor {

  def getDsPath(user: String, ds: String) = s"./.datastore/datasets/$user/$ds"

  private def handleALS(username: String, name: String, als: MnALS) = {
    val sparkWorker = als match {
      case MnALS(train: String, query: String, ranks: Int, maxIter: Int, id: String) => new VALS(getDsPath(username,train), ranks, maxIter)
    }

    mediator ! MinerResult(mining.Algorithm.ALS, username, name, sparkWorker.saveToObjectFile, als)
  }

  private def handleSVM(username: String, name: String, svm: MnSVM) = {
    val sparkWorker = svm match {
      case MnSVM(trin: String, test: String, maxIter: Int, id: String) => new VSVM(getDsPath(username, trin), getDsPath(username, test), maxIter)
    }

    mediator ! MinerResult(mining.Algorithm.SVM, username, name, sparkWorker.saveToObjectFile, svm)
  }

  private def handleFPM(username: String, name: String, fpm: MnFPM) = {
    val sparkWorker = fpm match {
      case MnFPM(name: String, minSupport: Double, id: String) => new VFPM(getDsPath(username,name), minSupport)
    }

    mediator ! MinerResult(mining.Algorithm.FPM, username, name, sparkWorker.saveToObjectFile, fpm)
  }

  private def handleClustering(username: String, name: String, clustering: MnClustering) = {
    val sparkWorker = clustering match {
      case MnClustering(name: String, test: Option[String], maxIter: Int, clusters: Int, id: String) =>
        new VClustering(getDsPath(username,name), test.map{getDsPath(username, _)}, clusters, maxIter)
    }

    mediator ! MinerResult(mining.Algorithm.Clustering, username, name, sparkWorker.saveToObject, clustering)
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

    case SubmitMineJob(username: String, job: MnALS) => handleALS(username, job.id, job)
    case SubmitMineJob(username: String, job: MnSVM) => handleSVM(username, job.id, job)
    case SubmitMineJob(username: String, job: MnFPM) =>
      println("Miner worker received MnFPgrowth")
      handleFPM(username, job.id, job)
    case SubmitMineJob(username: String, job: MnClustering) => handleClustering(username, job.id, job)
  }

}
