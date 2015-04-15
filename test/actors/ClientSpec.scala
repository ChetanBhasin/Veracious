package actors

import actors.client.Client._
import actors.mediator.RegisterForNotification
import akka.actor.PoisonPill
import models.messages.client._
import play.api.libs.json._

import scala.concurrent.duration._

/**
 * Created by basso on 10/04/15.
 */
class ClientSpec extends IntegrationTest {
  val user = "Anish"

  mediator ! RegisterForNotification (testActor)

  /** We shall become the web-socket for the client **/
  val client = system.actorOf(props(mediator)(user, testActor), "testClient")

  "Client" should "broadcast login message" in {
    expectMsg(LogIn(user))
  }

  val sampleJson = Json.obj ("user" -> "you", "name" -> "jibin")
  it should "forward json message to socket" in {
    mediator ! MessageToClient(user, sampleJson)
    expectMsg(sampleJson)
  }

  it should "ignore messages not meant for it" in {
    mediator ! MessageToClient("someOtherUser", sampleJson)
    expectNoMsg(1 second)
  }

  it should "ignore any other messages" in {
    client ! "Hellooo"
    expectNoMsg(1 second)
  }

  it should "broadcast logout message when killed" in {
    client ! PoisonPill
    expectMsg(LogOut(user))
  }
}
