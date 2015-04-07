package models.mining.oldAlgo

/**
 * Created by basso on 07/04/15.
 */
package object oldAlgo {

  private val conf = new SparkConf().setAppName("Veracious-Algorithms").setMaster("local")
  val sc = new SparkContext(conf)

}
