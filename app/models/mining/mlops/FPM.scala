package models.mining.mlops

import org.apache.spark.mllib.fpm.FPGrowth
import org.apache.spark.rdd.RDD

/**
 * Frequent pattern detection using FP-Growth algorithm
 * @param minSupport Minimum support for the itemsets
 * @param numPartitions Number of partitions to make on itemsets
 */
abstract class FPM(minSupport: Double = 0.2, numPartitions: Int = 10) {

  lazy val fpg = new FPGrowth()
    .setMinSupport(minSupport)
    .setNumPartitions(numPartitions)

  protected lazy val model = fpg.run(transactions)

  val transactions: RDD[Array[String]]

  def items = model.freqItemsets

  def getItemsetData = model.freqItemsets.collect.foreach {
    itemset =>
      (itemset.items.mkString("[", ",", "]"), itemset.freq)
  }

  def saveToTextFile(filePath: String) = model.freqItemsets.saveAsTextFile(filePath)

  def saveToObjectFile(filePath: String) = model.freqItemsets.saveAsObjectFile(filePath)

}

/**
 * FPM Algorithm on a file content
 * @param file File path
 * @param minSupport Minimum support for the itemsets
 * @param numPartitions Number of partitions to make on itemsets
 */
class fileFPM(file: String, minSupport: Double = 0.2, numPartitions: Int = 10)
  extends FPM(minSupport, numPartitions) {

  val transactions = sc.textFile("file").map(_.split(""))

}

/**
 * FPM Algorithm directly on the RDD
 * @param rdd RDD
 * @param minSupport Minimum support for the itemsets
 * @param numPartitions Number of partitions to make on itemsets
 */
class RDDSetFPM(rdd: RDD[Array[String]], minSupport: Double = 0.2, numPartitions: Int = 10)
  extends FPM(minSupport, numPartitions) {

  val transactions = rdd

}
