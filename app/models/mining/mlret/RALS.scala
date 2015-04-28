package models.mining.mlret

/**
 * Created by chetan on 28/04/15.
 */

/**
 * Read the ALS object file from the disk and output the data
 * @param filepath Path of the object file/folder
 */
class RALS(filepath: String) {

  val obj = sc.objectFile[((Int, Int), Double)](filepath).cache()

  def getOrderedData = obj.map {
    case ((user: Int, item: Int), rating: Double) => (item, rating)
  }.reduceByKey((x, y) => (x + y) / 2).map {
    case (item: Int, rate: Double) => (rate, item)
  }.sortByKey(true, -1).collect()

  def getRaw = obj.collect()

}
