package actors

import actors.client.Client._
import actors.mediator.RegisterForReceive
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

  "Client" should "register itself at mediator" in {
    mediator.expectMsgClass(classOf[RegisterForReceive])
  }

  "Client" should "broadcast login message" in {
    mediator.expectMsg(LogIn(user))
  }

  val sampleJson = Json.obj ("user" -> "you", "name" -> "jibin")
  it should "forward json message to socket" in {
    client ! MessageToClient(user, sampleJson)
    expectMsg(sampleJson)
  }

  it should "ignore messages not meant for it" in {
    client ! MessageToClient("someOtherUser", sampleJson)
    expectNoMsg()
  }

  it should "ignore any other messages" in {
    client ! "Hellooo"
    expectNoMsg(1 second)
  }

  it should "broadcast logout message when killed" in {
    client ! PoisonPill
    mediator.expectMsg(LogOut(user))
  }
}
