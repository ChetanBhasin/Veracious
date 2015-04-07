package models.batch.job

/**
 * Created by basso on 07/04/15.
 *
 * This file contains:
 *  1. The trait Job which is the grand-parent in the hierarchy
 *  2. The Job factory
 *  3. Marker traits for the two main categories of sub-classes for the job
 */


/** Design decision
  * Every job needs the Id. But this Id cannot be submitted at construction as
  * the mapping from the user form will be problematic.
  * Hence, a separate function needs to be given which will create a new job
  * (we need immutability of course) with the provided id. Usefull as we already
  * need to do a map on the initial job list to extract files from the request
  * (consult the desing docs)
  */
trait Job {
  val id: String                // Preferably BatchId:Job#
  def setId (id: String): Job   // Have to use the copy constructor of the case class but cannot implement here
}

trait DataSetOp extends Job     // Used by the DataSet operations manager
trait MineOp    extends Job     // Used by the mining subsystem

// TODO, implement the Job factory
