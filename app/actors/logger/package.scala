package actors

/**
 * Created by basso on 09/04/15.
 *
 * The Logger system is responsible for all the logging activities related to the end-user.
 * It maintains a single text file which contains the log of all the user events.
 *
 * API
 *  Accepts:
 *    Log(status: OperationStatus, username: String, msg: String, event: LogEvent)
 *      - Uses a specific format to log out the information in this message
 *
 * The System watches clients logging in and out of the application
 *
 * The Logger saves the log on disk in the format:
 *
 * { user: String,
 *   log: {
 *     status: String,
 *     message: String,
 *     activity: String
 *   }
 * }
 *
 * Clients receive the log message as :
 * { log : [ {status, message, activity} ] }
 */
package object logger {
}
