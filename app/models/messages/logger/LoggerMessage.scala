package models.messages.logger

import models.batch.OperationStatus.OperationStatus
import models.messages.client.PushData

/**
 * Created by basso on 08/04/15.
 * The set of classes that is sent to the logger system
 */

trait LoggerMessage

/**
 * Log class
 * @param status The status of the Operation as specified in batch.OperationStatus
 * @param user The user of the job
 * @param msg A short message description
 * @param content A logEvent type with the logWrite val/function
 */
case class Log (status: OperationStatus, user: String, msg: String, content: LogEvent) extends LoggerMessage with PushData

/**
 * Request starting list of logs for the given user
 * @param username
 */
case class GetLogs (username: String) extends LoggerMessage
