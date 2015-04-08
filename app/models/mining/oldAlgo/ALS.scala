package models.mining.oldAlgo

import org.apache.spark.mllib.recommendation.{ALS, Rating}
import org.apache.spark.rdd.RDD

/**
 * Created by chetan on 08/04/15.
 */


/**
 * ALS algorithm for collaborative filltering
 * @param rank
 * @param numIterations Number of iterations to be performed for the algorithm
 */
abstract class ALS(rank: Int = 10, numIterations: Int = 20) {

  protected[oldAlgo] def data: (RDD[String])

  lazy val ratings = data.map(_.split(",") match {
    case Array(user, item, rate) =>
      Rating(user.toInt, item.toInt, rate.toDouble)
  })

  lazy val userItems = ratings.map {
    case Rating(user, item, rate) => (user, item)
  }

  lazy val model = ALS.train(ratings, rank, numIterations)

  lazy val predictions = model.predict(userItems).map {
    case Rating(user, item, rate) =>
      ((user, item), rate)
  }

  def run = {
    println("models.mining.oldAlgo.ALS:")
    predictions.map { x =>
      x match {
        case ((user, item), rate) => println("User: " + user + "; Item: " + item + "; Rating: " + rate)
      }
    }
  }

  def saveToTextFile(filePath: String) = predictions.saveAsTextFile(filePath)

  def saveToObjectFile(filePath: String) = predictions.saveAsObjectFile(filePath)

}

/**
 * ALS algorithm on file contents
 * @param file Path of the file on which algorithm has to be performed
 * @param rank
 * @param numIterations Number of iterations to be performed for the algorithm
 */
class fileALS(file: String, rank: Int = 10, numIterations: Int = 20)
  extends ALS(rank, numIterations) {

  protected[oldAlgo] def data: RDD[String] = sc.textFile(file)

}

/**
 * ALS algorithm on RDD contents
 * @param rdd RDD on which the operations have to be performed
 * @param rank
 * @param numIterations Number of the iterations to be performed for the algorithm
 */
class RDDContentALS(rdd: RDD[String], rank: Int = 10, numIterations: Int = 20)
  extends ALS(rank, numIterations) {

  protected[oldAlgo] def data = rdd

}
