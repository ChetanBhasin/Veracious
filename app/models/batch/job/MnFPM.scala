package models.batch.job

/**
 * Created by basso on 07/04/15.
 */
case class MnFPM (
  ds_name: String,
  min_support: Double,
  id: String = " "
) extends MineOp {
  def setId (nid: String) = this.copy(id = nid)
  def logWrite = jobPrintFormat(id, "Frequent Pattern Matching", Map(
    "dataSet" -> ds_name,
    "min_support" -> min_support.toString))
}
