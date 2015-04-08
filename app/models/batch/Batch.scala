package models.batch

import java.io.{File, PrintWriter}
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicInteger

import models.batch.job.{DsAddDirect, Job}
import play.api.mvc.{AnyContent, Request}

import scala.io.Source

/**
 * Created by basso on 08/04/15.
 * The Batch class and it's Factory
 */

object Batch {

  /**
   * Setting up the Batch UID's.
   */
    // The file ./conf/runCount just keeps a simple counter for the current
    // application run number.
  val filePath = "./conf/runCount"
  private lazy val runCount = {   // Get the current run count
    try {
      Source.fromFile(filePath).getLines().toList.head.toInt
    } catch {
      case ex: Exception => 0
    }
  }

    // This is an atomic counter for concurrent access for counting the batch UID number
  private lazy val rNum = new AtomicInteger(0)

    // Get the UID for a Batch
  private def getUID = "B" + runCount + rNum.getAndIncrement

    // TODO: When the application closes, one of the major actors needs to invoke this function,
    // preferably the batch processor
  def storeRunCount() {
    val writer = new PrintWriter(new File(filePath))
    writer.write((runCount + 1).toString)
    writer.close()
  }
  /** ------------------------------------- **/

  /**
   * This is the factory call to create a Batch.
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
case class Batch (id: String, date: LocalDateTime, jobs: List[Job])
