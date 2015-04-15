package controllers

import play.api.mvc._

/**
 * Created by basso on 10/04/15.
 * Authorisation controller
 */

import play.api.data.Forms._
import play.api.data._

// TODO: create routes for the Auth

object Auth extends Controller {
  val loginForm = Form(
    tuple(
      "email" -> text,
      "password" -> text
    ) verifying ("Invalid email or password",  { case (email, password) => check(email, password)})
  )

  /** TODO: implement the actual user checking **/
  def check(username: String, password: String) =
    username == "admin" && password == "admin"

  def login = ???   //Ok(views.html.login(loginForm))

  def authenticate = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => BadRequest, //BadRequest(html.login(formWithErrors)),
      user => Redirect(routes.Application.index).withSession(Security.username -> user._1)
    )
  }

  def logout = Action {
    Redirect(routes.Auth.login).withNewSession.flashing(
      "success" -> "You are now logged out."
    )
  }
}

/** Trait to secure a controller's actions **/
trait Secured {
    /** Get username from the headers */
  def username(request: RequestHeader):Option[String] = request.session.get(Security.username)

    /** Redirection when unauthorised */
  def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Auth.login)

  def isAuthenticated(f: => String => Request[AnyContent] => Result) = {
    Security.Authenticated(username, onUnauthorized) { user => // Wrapper for the action
      Action(request => f(user)(request))
    }
  }
}
