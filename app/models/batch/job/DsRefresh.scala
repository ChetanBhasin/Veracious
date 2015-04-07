package models.batch.job

/**
 * Created by basso on 07/04/15.
 *
 * This class represents the refresh job where a previous data-set
 * which has the url present will be asked to refresh it's data-set from
 * the url
 */
case class DsRefresh (name: String, id: String = " ") extends DataSetOp {
  def setId(nid: String) = this.copy(id = nid)
}
