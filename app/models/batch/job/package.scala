package models.batch
import play.api.data.Forms._
import play.api.data._

/**
 * Created by basso on 07/04/15.
 *
 * This mlops contains the Job hierarchy. This is used to identify individual jobs in the
 * batch.
 *
 * The mlops also contains the required mapping for the user input form to job. (handled by the job factory)
 */
package object job {
  val jobListForm = Form(
    single(
      "jobs" -> list(
        mapping(
          "opType" -> nonEmptyText,
          "opName" -> nonEmptyText,
          "optionalTextParam" -> optional(text),
          "textParams" -> list(text),
          "numParams" -> list(number)
        )(Job.apply)(Job.unapply)
      )
    )
  )
}
