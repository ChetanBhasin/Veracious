package models.mining.mlret

import org.apache.spark.mllib.linalg.Vector

/**
 * Created by chetan on 28/04/15.
 */

/**
 * Read the K-Means clustering object file from the disk and display it to the user
 * @param filepath Path of the object file/folder
 */
class RClustering(filepath: String, name: String) extends MOutput {

  val obj = sc.objectFile[(Int, Vector)](filepath)

  def getData = obj.collect.toStream

  def output = ???

}