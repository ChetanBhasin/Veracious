package actorSpec

import actors.client.Client._
import akka.actor.PoisonPill
import models.messages.client._
import play.api.libs.json._

import scala.concurrent.duration._

/**
 * Created by basso on 10/04/15.
 */
class ClientSpec extends UnitTest {
  val user = "Anish"

  //mediator ! RegisterForNotification (testActor)

  /** We shall become the web-socket for the client **/
  val client = system.actorOf(props(mediator.ref)(user, testActor), "testClient")

  "Client" should "send login message to its manager" in {
    mediator.expectMsg(new LogIn(user) with ClientManagerMessage)
  }

  val sampleJson = Json.obj ("user" -> "you", "name" -> "jibin")
  it should "forward json message to socket" in {
    client ! Push(sampleJson)
    expectMsg(sampleJson)
  }

  it should "ignore any other messages" in {
    client ! "Hellooo"
    expectNoMsg(1 second)
  }

  it should "Send logout message to its manager when killed" in {
    client ! PoisonPill
    mediator.expectMsg(new LogOut(user) with ClientManagerMessage)
  }
}
