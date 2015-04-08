package models.batch.job

/**
 * Created by basso on 07/04/15.
 * Simple job to delete a given data-set
 */

case class DsDelete (name: String, id: String = " ") extends DataSetOp {
  def setId(nid: String) = this.copy(id = nid)
  def logWrite = jobPrintFormat(id, "Delete Data-set", Map("name" -> name))
}
