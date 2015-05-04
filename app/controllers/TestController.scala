package controllers

/**
 * Created by basso on 29/04/15.
 */
import models.security.loginForm
import play.api.libs.iteratee.{Enumerator, Iteratee}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global

object TestController extends Controller {

  def test = Action {
    Ok(views.html.testPage())
  }

  def testActual = Action {
    Ok(views.html.index())
  }

  def connectJs = Action { implicit request =>
    Ok(javascripts.js.testConnect(request))
  }

  def connect = WebSocket.using[JsValue] { request =>
    val in = Iteratee.foreach[JsValue](println).map { _ =>
        println("Disconnected")
    }
    //val out = Enumerator[JsValue](Json.obj("test" -> "hello"))
    val out = Enumerator[JsValue](
      Json.obj("logs" -> actors.logger.Logger.getLogs("Anish")("./test/resources/logFile_test_orig"))
    )
    (in, out)
  }

  import models.batch.Batch
  import models.batch.job.jobListForm
  def submitBatch = Action { implicit request =>
    println("DEBUG: Here is the request body :: " + request.body)
    jobListForm.bindFromRequest.fold (
      formWithErrors => {
        println("\n\nDEBUG: formWithErrors: ")
        println(formWithErrors.errors)
        BadRequest },
      jobList => {
        //println("Got the following Mapping :: "+jobList)
        println("Got the following batch : " + Batch(jobList, request))
        Ok
      }
    )
    //Ok(200)
  }

  def testLoginForm = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.testPage()),
      form => Ok("Got Login Form : "+form)
    )
  }
}
