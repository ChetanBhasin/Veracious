package models.batch.job

/**
 * Created by basso on 07/04/15.
 *
 * MineOp job for the ALS operation
 */
case class MnALS (
  ds_train: String,   // JSON file, space separated
  ds_query: String,   // JSON file, space separated
  ranks: Int,         // = 10
  max_iter: Int,      // = 20
  id: String = " "
) extends MineOp {
  def setId(nid: String) = this.copy(id = nid)
  def logWrite = jobPrintFormat(id, "ALS (Alternating Least Square) mining",
    Map("training_datSet" -> ds_train,
        "query" -> ds_query,
        "ranks" -> ranks.toString,
        "iterations" -> max_iter.toString))
}
