package controllers

import actors.application.AppRunning
import play.api.mvc._

/**
 * Created by basso on 10/04/15.
 * Authorisation controller
 */

import models.Application.appAccess
import models.security.loginForm

// TODO: create routes for the Auth

object Auth extends Controller {

  def check(username: String, password: String) =
    appAccess.authenticate(username, password)

  /** Here we need to check whether the application is ready or not */
  def login = Action {
    if (appAccess.appStatus == AppRunning)
      Ok(views.html.login())
    else Ok("Application is not running")
  }

  def authenticate = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.login()),
      lgForm =>
        if (lgForm.signUp.toBoolean)
          appAccess.signUp(lgForm.username, lgForm.password) match {
            case Left(str) => Redirect(routes.Auth.login).flashing("failure" -> str )
            case _ => Redirect(routes.Auth.login).flashing("success" -> "SignUp was successfull, now please login")
          }
        else if (check(lgForm.username, lgForm.password))
            Redirect(routes.Application.index).withSession(Security.username -> lgForm.username)  // If we have a Security.username, we are authenticated
        else Redirect(routes.Auth.login).flashing("failure" -> "Authentication Failure")
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
