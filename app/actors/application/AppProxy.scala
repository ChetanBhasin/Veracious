package actors.application

import actors.persistenceManager.UserManager
import akka.actor.FSM.{CurrentState, SubscribeTransitionCallBack, Transition}
import akka.actor.TypedActor.Receiver
import akka.actor.{ActorRef, TypedActor}
import akka.pattern.ask
import akka.util.Timeout
import models.batch.{Batch, OperationStatus}
import models.messages.application.AppShutDown
import models.messages.batchProcessing.SubmitBatch
import models.messages.client.UserAlreadyLoggedIn
import models.messages.persistenceManaging.GetUserManager

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

/**
 * Created by basso on 24/04/15.
 *
 * Typed actor to use on the system
 *
 * TODO: !! Important, this actor needs to have a Resume Strategy
 */
private[application] class AppProxy (val appManager: ActorRef, mediator: ActorRef) extends AppAccess with Receiver {
  var _appStatus: AppState = AppSetup
  appManager ! SubscribeTransitionCallBack(TypedActor.context.self)
  implicit val timeout = Timeout(10 seconds)

  def onReceive(msg: Any, sender: ActorRef) = msg match {
    case CurrentState(_, state: AppState) => _appStatus = state
    case Transition(_,_,to: AppState) => _appStatus = to
  }

  private var _userAuth: UserManager = null
  private def userAuth = (appStatus, _userAuth) match {
    case (AppRunning, null) =>
      _userAuth = Await.result(mediator ? GetUserManager, 10 seconds).asInstanceOf[UserManager]
      _userAuth
    case (AppRunning, userAuth) => userAuth
    case _ => throw new Exception ("Should not have asked for user manager while app is not running")
  }

  def appStatus = _appStatus

  def shutdown = appManager ! AppShutDown

  def authenticate(username: String, password: String) = Future(
    userAuth.authenticate(username, password)
  )

  val authError = Left("Authentication Failed")
  val opError = Left("Operation Failed")

  /*
  def removeUser(username: String, password: String) =
    if (authenticate(username, password))
      userAuth.removeUser(username) match {
        case OperationStatus.OpSuccess => Right(Unit)
        case _ => Left("Operation Failed")
      }
    else Left("Authentication Failed") */

  def removeUser(username: String, password: String) =
    authenticate(username, password).map {
      case true => userAuth.removeUser(username) match {
        case OperationStatus.OpSuccess => Right(Unit)
        case _ => opError
      }
      case false => authError
    }

  /*
  def changePassword(username: String, oldP: String, newP: String) =
    if (authenticate(username, oldP))
      userAuth.changePassword(username, newP) match {
        case OperationStatus.OpSuccess => Right(Unit)
        case _ => Left("Operation Failed")
      }
    else Left("Authentication Failed")
    */
  def changePassword(username: String, oldP: String, newP: String) =
    authenticate(username, oldP) map {
      case true => userAuth.changePassword(username, newP) match {
        case OperationStatus.OpSuccess => Right(Unit)
        case _ => opError
      }
      case false => authError
    }

  def signUp(username: String, password: String) = Future(
    if (userAuth.checkUsername(username))
      Left("User already exists")
    else
      userAuth.addUser(username, password) match {
        case OperationStatus.OpSuccess => Right(Unit)
        case _ => Left("Operation Failed")
      }
  )

  def alreadyLoggedIn (username: String) =
    Await.result(mediator ? UserAlreadyLoggedIn(username), 4 seconds).asInstanceOf[Boolean]

  def submitBatch (username: String, batch: Batch) =
    mediator ! SubmitBatch(username, batch)
}
