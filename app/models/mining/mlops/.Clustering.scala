package models.mining.mlops

/**
 * Created by chetan on 12/03/15.
 */

/**
 * models.mining.oldAlgo.Clustering using KMeans algorithm
 * @param numClusters Number of clusters required
 * @param numIterations Number of iterations required (defaults to 20)
 */
class VClustering(file: String, numClusters: Int, numIterations: Int = 20) {

  lazy val data: (RDD[String]) = sc.textFile(file)

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
