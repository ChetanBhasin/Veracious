package actors

/**
 * The Miner sub-system. Works on the Mining jobs
 *
 * API:
 *  Incoming:
 *    SubmitMineJob (user: String, job: MineOp)
 *  Outgoing:
 *    JobStatus (user, OperationStatus)  =>> Directly back to the batchProcessor.Worker that sent the SubmitMineJob
 *    Log(...)
 *    Result (...) =>> Sent to the mediator, TODO
 */
package object miner {

}
