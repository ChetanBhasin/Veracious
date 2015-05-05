package controllers

import akka.actor.ActorRef
import models.batch.Batch
import models.batch.job.jobListForm
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

  def submitBatch = isAuthenticated { username => implicit request =>
    jobListForm.bindFromRequest.fold (
      formWithErrors => BadRequest,
      jobList => {
        appAccess.submitBatch(username, Batch(jobList, request))     // TODO: Need a submit method on the app Access
        Status(200)
      }
    )
  }

  def getResult = isAuthenticated { username => implicit request =>
    request.body.asFormUrlEncoded.get.get("datasetName") match {
      case None => BadRequest
      case Some(seq) =>
        appAccess.requestResult(username, seq.head)
        Ok
    }
  }
  /*
  def index = isAuthenticated { username => implicit request =>
    Ok("helloo" + username)   // Test
  }*/

}