package models.mining

import org.apache.spark._

/**
 * Created by chetan on 28/04/15.
 */
package object mlret {

  val conf = try {
    new SparkConf().setAppName("Veracious-Retriver").setMaster("spark://Chetans-MacBook-Air.local:7077")
  } catch {
    case _: Throwable => new SparkConf().setAppName("Veracious-Tretriver").setMaster("local")
  }

  val sc = new SparkContext(conf)

}
