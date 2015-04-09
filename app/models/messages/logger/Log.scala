package models.messages.logger

import models.batch.OperationStatus.OperationStatus

/**
 * Created by basso on 08/04/15.
 *
 * This is the case class that is to be sent to the Logger system.
 */

/**
 * Log class
 * @param status The status of the Operation as specified in batch.OperationStatus
 * @param user The user of the job
 * @param msg A short message description
 * @param content A logEvent type with the logWrite val/function
 */
case class Log (status: OperationStatus, user: String, msg: String, content: LogEvent)
