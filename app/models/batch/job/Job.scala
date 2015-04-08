package models.batch.job

import models.mining.Algorithm

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
trait MineOp    extends Job {
  val ds_name: String
}

// TODO, implement the Job factory

/** The critical Job factory,
  * Used by the Form[List[Job]] mapping.
  */
object Job {

  /**
   * The apply function creates the correct kind of job for the user data
   * that is passed to the application as a form
   * @param opType : Gets in as a drop down list. Select the kind of major operation
   * @param opName : The actual operation name
   * @param optionalTextParam : Needed for Clustering, a single optional text parameter from the form
   * @param textParams : Extra text parameters are generically saved in an array. Make sure to use the correct mapping in the form
   * @param numParams : Extra numeric paramters again generically saved in an array
   * @return : An sub-type of class Job, to be saved in a batch
   */
   val apply = (
    opType: String,
    opName: String,
    optionalTextParam: Option[String],    // One of the algorithm needs this one
    textParams: List[String],             // Extra text parameters, may also contain Doubles as they aren't handled properly by Form
    numParams: List[Int]
  ) => opType match {
    case "DataSetOp" => opName match {      // Data set operations
      case "DsAddDirect" =>
        assert(textParams.length >= 3) // Important, not doing any checking here, TODO
        DsAddDirect(
          name = textParams(0),
          description = textParams(1),
          target_algo = Algorithm.withName(textParams(2)))
      case "DsAddFromUrl" =>
        assert(textParams.length >= 4)
        DsAddFromUrl(
          name = textParams(0),
          description = textParams(1),
          target_algo = Algorithm.withName(textParams(2)),
          url = textParams(3))
      case "DsDelete" =>
        assert(textParams.length >= 1)
        DsDelete(name = textParams(0))
      case "DsRefresh" =>
        assert(textParams.length >= 1)
        DsRefresh(name = textParams(0))
    }
    case "MineOp" => opName match {         // Mining Operations
      case "MnALS" =>
        assert(textParams.length >= 1)
        MnALS(ds_name = textParams(0))
      case "MnClustering" =>
        assert(textParams.length >= 1)
        assert(numParams.length >= 2)
        MnClustering(
          ds_name = textParams(0),
          pred_ds = optionalTextParam,
          max_iter = numParams(0),
          cluster_count = numParams(1))
      case "MnFPgrowth" =>
        assert(textParams.length >= 2)
        MnFPgrowth(
          ds_name = textParams(0),
          min_support = textParams(1).toDouble)
      case "MnSVM" =>
        assert(textParams.length >= 1)
        assert(numParams.length >= 1)
        MnSVM(
          ds_name = textParams(0),
          max_iter = numParams(0))
    }
  }

  val unapply = (job: Job) => job match {
    case DsAddDirect(ds, d, _, _, _) => Some("DataSetOp", "DsAddDirect", None, List(ds, d), List[Int]())
    case DsAddFromUrl(ds, d, _, u, _) => Some("DataSetOp", "DsAddDirect", None, List(ds, d, u), List[Int]())
    case DsDelete(ds) => Some("DataSetOp", "DsDelete", None, List(ds), List[Int]())
    case DsRefresh(ds) => Some("DataSetOp", "DsRefresh", None, List(ds), List[Int]())
    case MnALS(ds) => Some("MineOp", "MnALS", None, List(ds), List[Int]())
    case MnClustering(ds, pr, mit, cc) => Some("MineOp", "MnClustering", pr, List(ds), List(mit, cc))
    case MnFPgrowth(ds, ms) => Some("MineOp", "MnFPgrowth", None, List(ds, ms.toString), List[Int]())
    case MnSVM(ds, mit) => Some("MineOp", "MnSVM", None, List(ds), List(mit))
  }
}
