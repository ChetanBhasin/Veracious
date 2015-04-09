package models.batch.job

import models.mining.Algorithm.Algorithm
import play.api.libs.Files.TemporaryFile

/**
 * Created by basso on 07/04/15.
 * This is a DataSetOp which is used to add a new data-set to the system
 *
 * The DsAddDirect class presents complications as the file can be extracted
 * from the request only AFTER the initial mapping. Hence the file is left
 * out with a default null and later in a map operation, the file is extracted
 * and placed here.
 */

abstract class DsAdd extends DataSetOp {
  val name: String
  val description: String
  val target_algo: Algorithm
}


case class DsAddFromUrl (
  name: String,                     // Name of data-set to be entered
  description: String,              // Short description
  target_algo: Algorithm,           // Which algorithm it is intended for
  url: String,                      // url of the data-set
  id: String = " "                  // for id to be set up later
) extends DsAdd {
  def setId(nid: String) = this.copy(id = nid)       // Immutable structures
  def logWrite = jobPrintFormat(id, "Add data-set file from Url", Map("name" -> name, "url" -> url))
}

case class DsAddDirect (
  name: String,
  description: String,
  target_algo: Algorithm,
  file: TemporaryFile = null,        // Not storing the file to disk just yet
  id: String = " "
) extends DsAdd {
  def setId(nid: String) = this.copy(id = nid)
  def setFile(nfile: TemporaryFile) = this.copy(file = nfile)   // Set up the file later after the mapping
  def logWrite = jobPrintFormat(id, "Add data-set file", Map("name" -> name))
}

