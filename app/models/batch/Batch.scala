package models.batch

import java.io.{File, PrintWriter}
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicInteger

import models.batch.job.{DsAddDirect, Job}
import play.api.mvc.{AnyContent, Request}

import scala.io.Source

/**
 * Created by basso on 08/04/15.
 * The Batch class and it's Factory
 */

object Batch {

  /** ----------------------- Miscellaneous ----------------------------------- **/

  // The file that contains the run-count of the application
  private val filePath = "./conf/runCount"
  // Get the current run count
  private lazy val runCount = {
    try {
      val source = Source.fromFile(filePath)
      val res = source.getLines().toList.head.toInt + 1
      source.close()
      res
    } catch {
      case ex: Exception => 0
    }
  }

  /** used in the unique id creation. This number depicts the number of batches created till now - 1 **/
  private lazy val rNum = new AtomicInteger(0)

  /** create unique id for the batches. **/
  private def getUID = "B" + runCount + rNum.getAndIncrement

  /**
   * Store the application run count back to the file
   * TODO: A major actor (maybe the Batch processor) should call this function when the
   * application is closing
   */
  def storeRunCount() {
    val writer = new PrintWriter(new File(filePath))
    writer.write((runCount).toString)
    writer.close()
  }

    /** formatter used to print the date (inside Batch class) in the logs **/
  private val dateTimeFormat = "eee, dd MMM yyyy, hh:mm a"

  /** ---------------------------------------------------------------------------------------- **/

  /**
   * This is the factory method to create a Batch.
   * @param jobs The Job list from the Form binding. It contains incomplete Job classes with missing ids and files
   * @param request The request receivec by the controller (the same from which the initial Form was bound).
   * @return Batch containing a unique ID and the complete job list along with the dateTime of publish
   */
  def apply (jobs: List[Job], request: Request[AnyContent]): Batch = {
    val id = getUID
    val body = request.body.asMultipartFormData   // required to get the files from the request

    Batch (
      id,                   // The brand new id we got
      LocalDateTime.now(),  // Current DateTime
                            // Complete the job list by setting up the ids
      jobs.zipWithIndex.map {
        case (job, i) => (job.setId(id+"J"+(i+1)), i)   // The ID is the combo of batch id + job number
      }.flatMap {                                       // This flatMap operation will insert the temporary files uploaded by
        case (job: DsAddDirect, i) =>
          body.get.file("jobs["+i+"].file") match { // the DsAddDirect jobs into their objects
            case Some(fl) => Some(job.setFile(fl.ref))
            case None => None
          }
        case (other, _) => Some(other)
      }
    )   // Batch created
  }

}

/**
 * The actual Batch class, the object of which will be submitted to the Batch processor
 * @param id Unique id, created using the getUID function
 * @param date The Date and Time of the batch creation
 * @param jobs List of complete jobs inside the batch
 */
case class Batch (id: String, date: LocalDateTime, jobs: List[Job]) {
  import Batch._

  // important for it to be a val
  val logWrite = s"Batch:$id::${date.format(DateTimeFormatter.ofPattern(dateTimeFormat))}::numberOfJobs-${jobs.length}"
}
