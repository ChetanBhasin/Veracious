package models.batch
import play.api.data.Forms._
import play.api.data._

/**
 * Created by basso on 07/04/15.
 *
 * This package contains:
 *  1. The Job class hierarchy. This is used to identify individual jobs in the batch
 *  2. The Mapping for the user input from to Job. (Handled by the Job factory)
 *  3. The Job Factory object
 */
package object job {
  val jobListForm: Form[List[Job]] = Form(
    single(
      "jobs" -> list(
        mapping(
          "opType" -> text,
          "opName" -> text,
          "optionalTextParam" -> optional(text),
          "textParams" -> list(text),
          "numParams" -> list(number)
          // The last field is called "file" and will contain the file upload
        )(Job.apply)(Job.unapply)
      )
    )
  )

  def jobPrintFormat(id: String, desc: String, params: Map[String, String]) =
    s"Job:$id:$desc | " + params.map { case (k, v) => k + "-" + v }.mkString(",")
}
