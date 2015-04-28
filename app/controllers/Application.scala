package controllers

import akka.actor.ActorRef
import play.api.Play.current
import play.api.libs.json.JsValue
import play.api.mvc._

object Application extends Controller with Secured {
  import models.Application.appAccess

  def index = isAuthenticated { username => implicit request =>
    Ok(views.html.index())      // TODO,Don't know if session is maintained
  }

  def connectJs = Action { implicit request =>
    Ok(javascripts.js.connect(request))
  }

  def connect = WebSocket.acceptWithActor[JsValue, JsValue] {
    request => out: ActorRef =>
        username(request) match {
          case Some(user) if !appAccess.alreadyLoggedIn(user) => models.Application.clientProps(user, out)
          case _ => actors.client.UnAuthClient.props(out)
        }
  }

  /*
  def index = isAuthenticated { username => implicit request =>
    Ok("helloo" + username)   // Test
  }*/

}