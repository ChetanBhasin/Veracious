package actors

/**
 * The DsOperator Sub-system works on Job type DataSetOp
 * API:
 *  Incoming:
 *    SubmitDsOpJob (user: String, job: DataSetOp)
 *  Outgoing:
 *    JobStatus (user, OperationStatus)  =>> Directly back to the batchProcessor.Worker that sent the SubmitDsOpJob
 *    Log(...)
 *    MessageToClient (user, JsValue)
 *      Sent during a client Logs in, or whenever a data-set operation is performed. TODO: define the JsValue
 */
package object dsOperator {
}
