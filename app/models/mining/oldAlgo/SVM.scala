package models.mining.oldAlgo

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
abstract class SVM(numIterations: Int = 100) {

  protected[oldAlgo] def data: RDD[LabeledPoint]

  protected[oldAlgo] def tests: RDD[org.apache.spark.mllib.linalg.Vector]

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

/**
 * Linear SVM algorithm on contents of a file
 * @param file Path of the vector file on which the algorithm has to be run
 * @param testFile Path of the file on which tests have to be performed
 * @param numIterations Number of iterations to be performed for the algorithm
 */
class fileSVM(file: String, testFile: String, numIterations: Int = 100) extends SVM(numIterations) {

  protected[oldAlgo] def data = MLUtils.loadLibSVMFile(sc, file)

  protected[oldAlgo] def tests = MLUtils.loadVectors(sc, testFile)

}

/**
 * Linear SVM algorithm on an RDD vector
 * @param rdd String RDD from which the vectors have to be extracted
 * @param testRDD RDD for test data
 * @param numIterations Number of iterations to be performed for the algorithm
 */
class RDDVectorSVM(rdd: RDD[LabeledPoint], testRDD: RDD[org.apache.spark.mllib.linalg.Vector], numIterations: Int) extends SVM(numIterations) {

  protected[oldAlgo] def data = rdd

  protected[oldAlgo] def tests = testRDD

}
