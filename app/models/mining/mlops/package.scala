package models.mining

/**
 * Created by chetan on 08/04/15.
 */
package object mlops {

  import org.apache.spark._

  lazy val conf: SparkConf = try {
    new SparkConf().setAppName("Veracious-Algorithms").setMaster("spark://Chetans-MacBook-Air.local:7077")
  } catch {
    case _: Throwable => new SparkConf().setAppName("Veracious-Algorithms").setMaster("local")
  }
  val sc = new SparkContext(conf)

}
