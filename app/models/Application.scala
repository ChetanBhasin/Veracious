package models

/**
 * Main object for the application
 */

import actors.application.{AppAccess, ApplicationManager}
import actors.client.Client
import actors.mediator.Mediator
import akka.actor._
import play.api.Play.current
import play.api.libs.concurrent.Akka.system        // Implicit currect application context

object Application {
  private val mediator = system.actorOf(Props[Mediator], "Mediator")
  private val appManager = system.actorOf(Props(classOf[ApplicationManager], mediator), "AppManager")

  val clientProps = Client.props(mediator)_
  /** We use this to access the application **/
  val appAccess: AppAccess = AppAccess(system, appManager, mediator, "AppAccess")
}
