package intSpec

import actors.application.{AppAccess, AppRunning}
import actors.persistenceManager.Persistence
import akka.actor.FSM.{CurrentState, SubscribeTransitionCallBack}
import akka.actor.{Props, TypedActor}
import akka.pattern._
import akka.testkit.TestProbe

import scala.concurrent.Await
import scala.concurrent.duration._

/**
 * Created by basso on 05/05/15.
 *
 * Integration test for AppAccess and Persistence
 * Checking the validity of userManager
 */
class AuthenticationSpec extends IntegrationTest {

  val user = "Anish"
  val pass = "Blah"

  val appManager = TestProbe()
  import actorSpec.AppAccessSpec.{Supervisor => tempSuper}
  implicit val timeout = akka.util.Timeout(10 seconds)

  val persistence = system.actorOf(Props(classOf[Persistence], mediator), "testPersistence")
  val parent = system.actorOf(Props(classOf[tempSuper], appManager.ref, mediator), "testAppAccess")
  val appAccess = Await.result(parent ? "get", 3 seconds).asInstanceOf[AppAccess]

  val sendMsg = TypedActor(system).getActorRefFor(appAccess) ! _

  "AppAccess" should "subscribe to transitions on appManager" in {
    appManager.expectMsgClass (classOf[SubscribeTransitionCallBack])
  }

  it should "change its state when received a current state message" in {
    sendMsg (CurrentState(appManager.ref, AppRunning))
    appAccess.appStatus shouldBe AppRunning
  }

  it should "get hold of the user manager for an authentication request" in {
    Await.result (appAccess.authenticate(user, pass), timeout.duration) shouldBe false
  }
}
