package models.batch.job

/**
 * Created by basso on 07/04/15.
 *
 * MineOp job for SVM algorithm
 */
case class MnSVM (
  ds_train: String,   // training vector file
  ds_test: String,    // testing vector file
  max_iter: Int,      // default to 100 (impl at user end)
  id: String = " "
) extends MineOp {
  def setId(nid: String) = this.copy(id = nid)
}
