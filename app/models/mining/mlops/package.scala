package models.mining

/**
 * Created by chetan on 08/04/15.
 */
package object mlops {

  import org.apache.spark._

  val confmain = new SparkConf().setAppName("Veracious-App").setMaster("spark://Chetan-MacBook-Air.local:7077")
  val confother = new SparkConf().setAppName("Veracious-App").setMaster("local")

  /*val sc: SparkContext = try {
    new SparkContext(confmain)
  } catch {
    case _: Throwable => new SparkContext(confother)
  }*/
  val sc = new SparkContext(confother)

}
