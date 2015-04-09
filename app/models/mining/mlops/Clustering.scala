package models.mining.mlops

import org.apache.spark.mllib.clustering._
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
abstract class Clustering(numClusters: Int, numIterations: Int = 20) {

  protected[mlops] def data: (RDD[String])

  private lazy val supply = data.map(Vectors.parse(_))
  lazy val clusters = KMeans.train(supply, numClusters, numIterations)

  def run = println("models.mining.mlops.Clustering:\n" + clusters.toString)

  def saveToTextFile(fileLocation: String) = {
    val dataset = clusters.predict(supply) zip supply
    dataset.saveAsTextFile(fileLocation)
  }

  def saveToObject(fileLocation: String) = {
    val dataset = clusters.predict(supply) zip supply
    dataset.saveAsObjectFile(fileLocation)
  }

}

/**
 * models.mining.oldAlgo.Clustering with a file storing vector values
 * @param file Location of the file with values
 * @param numClusters Number of clusters required
 * @param numIterations Number of iterations required (defaults to 20)
 */
class fileVectorClustering(file: String, numClusters: Int, numIterations: Int = 20)
  extends Clustering(numClusters, numIterations) {

  protected[mlops] def data = sc.textFile(file)

}

/**
 * models.mining.oldAlgo.Clustering with a String RDD
 * @param rdd
 * @param numClusters Number of clusters required
 * @param numIterations Number of iterations required (defaults to 20)
 */
class RDDVectorClustering(rdd: RDD[String], numClusters: Int, numIterations: Int = 20)
  extends Clustering(numClusters, numIterations) {

  protected[mlops] def data = rdd

}
