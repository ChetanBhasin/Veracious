package models

import play.api.data.Forms._
import play.api.data._

/**
 * Created by basso on 27/04/15.
 */
package object security {
  val loginForm: Form[LoginForm] = Form(
    mapping(
      "username" -> nonEmptyText,
      "password" -> nonEmptyText,
      "signUp" -> nonEmptyText
    )(LoginForm.apply)(LoginForm.unapply)
  )
}
