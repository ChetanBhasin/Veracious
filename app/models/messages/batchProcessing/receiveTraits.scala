package models.messages.batchProcessing

/** All messages which need to be sent to the Batch processor needs to mix in this trait */
trait BatchProcessorMessage

/** All messages to the Minor **/
trait MinerMessage

/** All messages to the DSoperator */
trait DsOperatorMessage
