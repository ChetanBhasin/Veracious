package controllers

import play.api.mvc._

object Application extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

  /*
  def index = isAuthenticated { username => implicit request =>
    Ok("helloo" + username)   // Test
  }*/

}