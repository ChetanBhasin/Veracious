package models.mining.mlret

import org.apache.spark.mllib.fpm.FPGrowth.FreqItemset
import org.apache.spark.rdd.RDD
import play.api.libs.json._

/**
 * Created by chetan on 28/04/15.
 */

/**
 * Retrive the FPM object file from the disk and output the data
 * @param filepath Path of the object file/folder
 */
class RFPM(filepath: String, name: String) extends MOutput {

  // Load the object from the disk
  lazy val obj: RDD[FreqItemset[String]] = sc.objectFile[FreqItemset[String]](filepath).cache()

  // Get the data and put it out
  lazy val getItemSet: Seq[(Array[String], Long)] = {
    obj.map {
      items => (items.freq, items.items)
    }.sortByKey(true).map(x => (x._2, x._1)).collect.toSeq
  }

  def tail = Json.toJson(getItemSet.map(x => JsObject(Seq(
    "label" -> JsString(x._1.mkString("[", ",", "]")),
    "y" -> JsNumber(x._2)))))

  // Json object that would be passed to the front end visual engine
  def output = JsObject(Seq(
    "name" -> JsString(name),
    "algorithm" -> JsString("fpm"),
    "data" -> tail
  ))

}