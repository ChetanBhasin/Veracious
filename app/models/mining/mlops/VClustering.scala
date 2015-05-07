package models.mining.mlops

import org.apache.spark.mllib.clustering.KMeans
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.rdd.RDD

/**
 * Created by chetan on 12/03/15.
 */

/**
 * models.mining.oldAlgo.Clustering using KMeans algorithm
 * @param numClusters Number of clusters required
 * @param numIterations Number of iterations required (defaults to 20)
 */
class VClustering(file: String, test: Option[String], numClusters: Int, numIterations: Int = 20) {

  lazy val data: (RDD[String]) = sc.textFile(file)

  private lazy val supply = data.map {
    line => Vectors.dense(line.split(' ').map(_.toDouble))
  }.cache()

  private lazy val preds = {
    test match {
      case Some(element: String) => sc.textFile(element).map(line => Vectors.dense(line.split(' ').map(_.toDouble)))
      case None => supply
    }
  }

  lazy val clusters = KMeans.train(supply, numClusters, numIterations)

  def run = println("models.mining.mlops.Clustering:\n" + clusters.toString)

  def saveToTextFile(fileLocation: String) = {
    val dataset = clusters.predict(supply) zip preds
    dataset.saveAsTextFile(fileLocation)
  }

  def saveToObject(fileLocation: String) = {
    val dataset = clusters.predict(supply) zip preds
    dataset.saveAsObjectFile(fileLocation)
  }

}
