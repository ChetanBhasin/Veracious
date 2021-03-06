package actorSpec

import _root_.mocks.MockUserManager
import actors.application._
import akka.actor.FSM.{CurrentState, SubscribeTransitionCallBack, Transition}
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import models.messages.client.{AskForResult, UserAlreadyLoggedIn}
import models.messages.persistenceManaging.GetUserManager

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.{Left, Right}
/**
 * Created by basso on 25/04/15.
 */

object AppAccessSpec {
  // Just because AppProxy throws exceptions and for testing we need that
  // it doesn't restart
  class Supervisor(appManager: ActorRef, mediator: ActorRef) extends Actor {
    import akka.actor.OneForOneStrategy
    import akka.actor.SupervisorStrategy._

    override val supervisorStrategy =
      OneForOneStrategy() {
        case _: Exception => Resume
      }

    val appAccess: AppAccess = AppAccess(context, appManager, mediator, "testAccess")

    def receive = {
      case msg => sender ! appAccess
    }
  }
}


class AppAccessSpec extends UnitTest {
  val user = "Anish"
  val pass = "Blah"
  val pass2 = "blublu"

  val appManager = mediator     // TestProbe
  implicit val timeout = Timeout(3 seconds)

  val parent = system.actorOf(Props(classOf[AppAccessSpec.Supervisor], appManager.ref, mediator.ref))
  val appAccess = Await.result(parent ? "get", 3 seconds).asInstanceOf[AppAccess]

  val sendMsg = TypedActor(system).getActorRefFor(appAccess) ! _

  "The Proxy" should "subscribe to transitions on appManager" in {
    appManager.expectMsgClass (classOf[SubscribeTransitionCallBack])
  }

  it should "initially set appState to AppSetup" in {
    appAccess.appStatus shouldBe AppSetup
  }

  it should "throw exception when requesting access while app is in setup" in {
    an [Exception] should be thrownBy Await.result(appAccess.authenticate(user, pass), 3 seconds)
  }

  it should "change its state when received a current state message" in {
    sendMsg (CurrentState(appManager.ref, AppFinish))
    appAccess.appStatus shouldBe AppFinish
  }

  it should "throw exception when requesting access while app is finishing" in {
    an [Exception] should be thrownBy Await.result(appAccess.authenticate(user, pass), 3 seconds)
  }

  it should "change state when received a transition state message" in {
    sendMsg (Transition(appManager.ref, AppFinish, AppRunning))     // Hypothetically speaking
    appAccess.appStatus shouldBe AppRunning
  }

  var resAuth: Future[Boolean] = null
  it should "ask for userManager the first time it needs it" in {
    resAuth = appAccess.authenticate(user, pass)
    mediator.expectMsg(GetUserManager)
  }

  it should s"authenticate $user once it gets its userManager" in {
    mediator.reply(new MockUserManager)
    Await.result(resAuth, 3 seconds) shouldBe true
  }

  it should s"not ask for userManager in subsequent operations and should change the password for $user" in {
    Await.result(appAccess.changePassword(user, pass, pass2), 3 seconds) shouldBe Right(())
    mediator.expectNoMsg(1 second)
  }

  it should s"give authentication failure message when trying to change passwords for $user" in {
    Await.result(appAccess.changePassword(user, pass, pass2), 3 seconds) shouldBe Left("Authentication Failed")
  }

  it should s"give authentication failure when trying to remove $user with wrong pass" in {
    Await.result(appAccess.removeUser(user, pass), 3 seconds) shouldBe Left("Authentication Failed")
  }

  it should s"successfully remove $user with correct pass" in {
    Await.result(appAccess.removeUser(user, pass2), 3 seconds) shouldBe Right(())
  }

  it should "bar us from signing up a username that already exists" in {
    Await.result(appAccess.signUp("Jibin", pass2), 3 seconds) shouldBe Left("User already exists")
  }

  it should "let us sign up a new user" in {
    Await.result(appAccess.signUp("Jerry", pass2), 3 seconds) shouldBe Right(())
  }

  it should "Ask client manager if user is already logged in" in {
    val res = Future(appAccess.alreadyLoggedIn(user))
    mediator.expectMsg(UserAlreadyLoggedIn(user))
    mediator.reply(true)
    Await.result(res, 3 seconds) shouldBe true
  }

  it should "make the client manager ask for result" in {
    appAccess.requestResult(user, "blah")
    mediator.expectMsg(AskForResult(user, "blah"))
  }
}
