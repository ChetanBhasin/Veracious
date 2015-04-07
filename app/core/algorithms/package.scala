package core

/**
 * Created by chetan on 24/03/15.
 */
package object algorithms {

  import org.apache.spark.{SparkContext, _}

  private val conf = new SparkConf().setAppName("Veracious-Algorithms").setMaster("local")
  val sc = new SparkContext(conf)

}
