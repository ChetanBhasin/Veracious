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

  def getData = obj.collect()

  def tail = JsObject(getData.toSeq.map(x => x._1.toString -> Json.toJson(x._2.toArray.toList)))

  def output = JsObject(Seq(
    "name" -> JsString(name),
    "algorithm" -> JsString("svm"),
    "data" -> tail
  ))

}