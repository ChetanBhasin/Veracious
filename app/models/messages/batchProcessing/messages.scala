package models.messages.batchProcessing

import models.batch.Batch
import models.batch.OperationStatus.OperationStatus
import models.batch.job.{DataSetOp, MineOp}

/**
 * This message will be sent from the Interpretter to the Batch processor
 */
case class SubmitBatch (username: String, batch: Batch) extends BatchProcessorMessage

/**
 * Used by the Miner and DsOperator to return the status of the last operation.
 * Given to the main controller in the Batch processor which then routes the status to
 * the correct worker
 */
case class JobStatus (username: String, status: OperationStatus) extends BatchProcessorMessage

/**
 * Submission classes
 */
case class SubmitMineJob (username: String, job: MineOp) extends MinerMessage
case class SubmitDsOpJob (username: String, job: DataSetOp) extends DsOperatorMessage


/** Notes
  The Batch Processor will log the result of the whole batch being processed.
  The Miner and DsOperator are responsible for loggint of their respective jobs.
  */