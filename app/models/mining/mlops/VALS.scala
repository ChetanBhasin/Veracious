package models.mining.mlops

import org.apache.spark.mllib.recommendation.Rating
import org.apache.spark.rdd.RDD

/**
 * Created by chetan on 08/04/15.
 */


/**
 * ALS algorithm for collaborative filltering
 * @param rank
 * @param numIterations Number of iterations to be performed for the algorithm
 */
class VALS(file: String, rank: Int = 10, numIterations: Int = 20) {

  lazy val data: (RDD[String]) = sc.textFile(file)

  lazy val ratings = data.map(_.split(",") match {
    case Array(user, item, rate) =>
      Rating(user.toInt, item.toInt, rate.toDouble)
  })

  lazy val userItems = ratings.map {
    case Rating(user, item, rate) => (user, item)
  }

  lazy val model = org.apache.spark.mllib.recommendation.ALS.train(ratings, rank, numIterations)

  lazy val predictions = model.predict(userItems).map {
    case Rating(user, item, rate) =>
      ((user, item), rate)
  }

  def run = {
    println("models.mining.mlops.ALS:")
    predictions.map { x =>
      x match {
        case ((user, item), rate) => println("User: " + user + "; Item: " + item + "; Rating: " + rate)
      }
    }
  }

  def saveToTextFile(filePath: String) = predictions.saveAsTextFile(filePath)

  def saveToObjectFile(filePath: String) = predictions.saveAsObjectFile(filePath)

}
