package actors

/**
 * Created by basso on 10/04/15.
 *
 * The batch processor sub-system is responsible for processing of individual batches.
 * It contains a parent processor/scheduler and multiple workers (1 for each user)
 */
package object batchProcessor {
  // This will contain subsystem level messages
  case class IAmFree(user: String)
}
