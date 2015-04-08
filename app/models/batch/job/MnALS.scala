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
}
