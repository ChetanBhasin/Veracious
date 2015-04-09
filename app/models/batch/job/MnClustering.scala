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
  def logWrite = jobPrintFormat(id, "Clustering", Map(
    "dataSet" -> ds_name,
    "prediction_DataSet" -> {
      pred_ds match {
        case Some(pds) => pds
        case None => "<None>"
      }},
    "iterations" -> max_iter.toString,
    "cluster_count" -> cluster_count.toString
  ))

}

