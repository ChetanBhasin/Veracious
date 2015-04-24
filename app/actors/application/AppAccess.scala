package actors.application

import akka.actor.ActorRef

/**
 * Created by basso on 24/04/15.
 * This is the Trait for the Typed actor which will give access to the app including
 * security
 */
trait AppAccess {
  val appManager: ActorRef      // An actorRef for Application Manager

  def appStatus: AppState

  def authenticate(username: String, password: String): Boolean
  def removeUser(username: String, password: String): Either[String, Unit]
  def changePassword(username: String, oldP: String, newP: String): Either[String, Unit]
  def signUp(username: String, password: String): Either[String, Unit]

  def shutdown: Unit
}
