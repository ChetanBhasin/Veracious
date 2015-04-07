package models.batch.job

/**
 * Created by basso on 07/04/15.
 *
 * A MineOp job for clustering algorithm
 */
case class MnClustering (
  ds_name: String,
  pred_ds: Option[String],
  max_iter: Int,
  cluster_count: Int,
  id: String = " "
) extends MineOp {
  def setId(nid: String) = this.copy(id = nid)
}

