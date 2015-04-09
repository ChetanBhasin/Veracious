package models.messages.batchProcessing

import models.batch.Batch
import models.batch.OperationStatus.OperationStatus
import models.batch.job.{DataSetOp, MineOp}

/**
 * This message will be sent from the Interpretter to the Batch processor
 */
case class SubmitBatch (username: String, batch: Batch) extends BatchProcessorMessage

/**
 * The next two classes will be used by the Miner and the DsOperator respectively
 * to return the status of the job they were given
 *
 * Actually, these classes will be sent back to the Batch processor directly instead of
 * being routed through the mediator
 */
case class MineStatus (username: String, status: OperationStatus) extends BatchProcessorMessage
case class DsOpStatus (username: String, status: OperationStatus) extends BatchProcessorMessage

/**
 * Submission classes
 */
case class SubmitMineJob (username: String, job: MineOp) extends MinerMessage
case class SubmitDsOpJob (username: String, job: DataSetOp) extends DsOperatorMessage


/** Notes
  The Batch Processor will log the result of the whole batch being processed.
  The Miner and DsOperator are responsible for loggint of their respective jobs.
  */