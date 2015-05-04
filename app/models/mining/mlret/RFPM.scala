package models.mining.mlret

import org.apache.spark.mllib.fpm.FPGrowth.FreqItemset
import org.apache.spark.rdd.RDD

/**
 * Created by chetan on 28/04/15.
 */

/**
 * Retrive the FPM object file from the disk and output the data
 * @param filepath Path of the object file/folder
 */
class RFPM(filepath: String) extends MOutput {

  // Load the object from the disk
  lazy val obj: RDD[FreqItemset[String]] = sc.objectFile[FreqItemset[String]](filepath)

  // Get the data and put it out
  def getItemSet = {
    obj.collect.foreach {
      items => (items.items, items.freq)
    }
  }

  def output = ???

}