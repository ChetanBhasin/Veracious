package controllers

import akka.actor.ActorRef
import play.api.Play.current
import play.api.libs.json.JsValue
import play.api.mvc._

object Application extends Controller with Secured {

  def index = Action {
    Ok(views.html.index())
  }

  def connectJs = Action { implicit request =>
    Ok(javascripts.js.connect(request))
  }

  // TODO: Conundrum: Need to secure this but how
  /* Maybe we don't need to secure this, only the when you reach index can you
     find this url.. but then, will you get the session security??
   */
  def connect = WebSocket.acceptWithActor[JsValue, JsValue] {
    request => out: ActorRef =>
        username(request) match {
          case Some(user) => models.Application.clientProps(user, out)
          case None => actors.client.UnAuthClient.props(out)
        }
  }

  /*
  def index = isAuthenticated { username => implicit request =>
    Ok("helloo" + username)   // Test
  }*/

}