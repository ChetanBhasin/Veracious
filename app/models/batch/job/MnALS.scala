package models.batch.job

/**
 * Created by basso on 07/04/15.
 *
 * MineOp job for the ALS operation
 */
case class MnALS (
  ds_name: String,
  id: String = " "
) extends MineOp {
  def setId(nid: String) = this.copy(id = nid)
}
