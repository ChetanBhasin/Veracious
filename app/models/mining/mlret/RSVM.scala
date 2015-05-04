package models.mining.mlret

import org.apache.spark.mllib.linalg

/**
 * Created by chetan on 28/04/15.
 */

/**
 * Read the SVM object file from the disk and output the data
 * @param filepath Path of the object file/folder
 */
class RSVM(filepath: String) extends MOutput {

  val obj = sc.objectFile[(Double, linalg.Vector)](filepath)

  val getData = obj.collect()

  def output = ???

}