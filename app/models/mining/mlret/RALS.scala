package models.mining.mlret

import play.api.libs.json.{JsNumber, JsObject, JsString}

/**
 * Created by chetan on 28/04/15.
 */

/**
 * Read the ALS object file from the disk and output the data
 * @param filepath Path of the object file/folder
 */
class RALS(filepath: String, name: String) extends MOutput {

  val obj = sc.objectFile[((Int, Int), Double)](filepath).cache()

  def getOrderedData: Array[(Double, Int)] = obj.map {
    case ((user: Int, item: Int), rating: Double) => (item, rating)
  }.reduceByKey((x, y) => (x + y) / 2).map {
    case (item: Int, rate: Double) => (rate, item)
  }.sortByKey(true, -1).collect()

  def getRaw = obj.collect()

  def tail = JsObject(getOrderedData.toSeq.map(x => x._1.toString -> JsNumber(x._2)))

  def output = JsObject(Seq(
  "name" -> JsString(name),
  "algorithm" -> JsString("fpm"),
  "data" -> tail
  ))

}