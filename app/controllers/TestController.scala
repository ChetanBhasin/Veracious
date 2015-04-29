package controllers

/**
 * Created by basso on 29/04/15.
 */
import models.security.loginForm
import play.api.mvc._

object TestController extends Controller {

  def test = Action {
    Ok(views.html.testPage())
  }

  def testLoginForm = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.testPage()),
      form => Ok("Got Login Form : "+form)
    )
  }
}
