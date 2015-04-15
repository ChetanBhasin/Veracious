package actors

/**
 * The batch processor sub-system is responsible for processing of individual batches.
 * It contains a parent processor/scheduler and multiple workers (1 for each user)
 *
 * The API for the batchProcessor is :
 * Accepts:
 *  SubmitBatch (user: String, batch: Batch)
 *      - Submit the given batch over for processing. No reply to this message. The results will be logged and saved
 *  JobStatus (user: String, status: OperationStatus)
 *      - The Mining sub-system and the DsOperations sub-system are supposed to send this message, one for each job received.
 *        No reply to this message.
 *
 * Sends:
 *  SubmitDsOpJob (user: String, job: DataSetOp)
 *      - This message is aimed for the DsOperation subsystem. Once the Job is complete/aborted, a JobStatus message is to be sent
 *        to the batch Processor
 *  SubmitMineJob (user: String, job: MineOp)
 *      - Similar to SubmitDsJob and is aimed for the Miner subystem. Expects a JobStatus once complete.
 *  Log(...)
 *      - It Logs the result of complete batch operations but not individual jobs.
 *  SyError(system: String, msg: String)
 *      - Has multiple cases of errors
 *
 * This subsystem also watches the clients logging in and out (LogIn() and LogOut() messages)
 */
package object batchProcessor {
  // This will contain subsystem level messages
  case class IAmFree(user: String)
}
