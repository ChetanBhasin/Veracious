package models.mining.mlret

import org.apache.spark.mllib.linalg
import play.api.libs.json._

/**
 * Created by chetan on 28/04/15.
 */

/**
 * Read the SVM object file from the disk and output the data
 * @param filepath Path of the object file/folder
 */
class RSVM(filepath: String, name: String) extends MOutput {

  val obj = sc.objectFile[(Double, linalg.Vector)](filepath)

  def getData = obj.collect().map(x => (x._1, x._2.toArray))

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
    "algorithm" -> JsString("svm"),
    "data" -> tail
  ))

}