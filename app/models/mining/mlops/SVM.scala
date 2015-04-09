package models.mining.mlops

import org.apache.spark.mllib.classification.SVMWithSGD
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.rdd.RDD

/**
 * Created by chetan on 08/04/15.
 */

/**
 * Linear SVM algorithm for binary classification
 * @param numIterations Number of iterations to be performed for the algorithm
 */
class SVM(file: String, testFile: String, numIterations: Int = 100) {

  lazy val data: RDD[LabeledPoint] = MLUtils.loadLibSVMFile(sc, file)

  lazy val tests: RDD[org.apache.spark.mllib.linalg.Vector] = MLUtils.loadVectors(sc, testFile)

  lazy val model = SVMWithSGD.train(data.cache, numIterations)

  model.clearThreshold()

  val predictions = tests.map {
    point =>
      val score = model.predict(point)
      (score, point)
  }

  def run = {
    println("models.mining.oldAlgo:")
    predictions.map {
      item => println(item._1.toString + " : " + item._2.toString)
    }
  }

  def saveToTextFile(filePath: String) = predictions.saveAsTextFile(filePath)

  def saveToObjectFile(filePath: String) = predictions.saveAsObjectFile(filePath)
}
