package models.mining.mlret

import org.apache.spark.mllib.linalg.Vector
import play.api.libs.json._

/**
 * Created by chetan on 28/04/15.
 */

/**
 * Read the K-Means clustering object file from the disk and display it to the user
 * @param filepath Path of the object file/folder
 */
class RClustering(filepath: String, name: String) extends MOutput {

  val obj = sc.objectFile[(Int, Vector)](filepath).cache()

  def getData: Array[(Int, Array[Double])] = obj.collect.map(x => (x._1, x._2.toArray))

  def me = getData.map {
    x =>
      JsObject(Seq("name" -> JsString(x._1.toString),
        "x" -> JsNumber(x._2(0)),
        "y" -> JsNumber(x._2(1))
      ))
  }.toSeq

  def tail = Json.toJson(me)

  def output = JsObject(Seq(
    "name" -> JsString(name),
    "algorithm" -> JsString("clustering"),
    "data" -> tail
  ))

}