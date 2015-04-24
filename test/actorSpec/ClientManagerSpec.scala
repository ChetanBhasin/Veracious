package actorSpec

import actorSpec.mocks.sampleLog
import actors.client.ClientManager
import actors.mediator.RegisterForReceive
import akka.actor.{Props, Terminated}
import akka.testkit.TestProbe
import models.messages.application.{FinishWork, FinishedWork, Ready}
import models.messages.batchProcessing.BatchProcessorMessage
import models.messages.client._
import models.messages.logger.GetLogs
import models.messages.persistenceManaging.{GetUserDataSets, GetUserResults}
import play.api.libs.json._

/**
 * Created by basso on 22/04/15.
 */
class ClientManagerSpec extends UnitTest {
  val user = "Anish"
  val parent = setupParent( Props(classOf[ClientManager], mediator.ref))

  "Client Manager" should "Setup correctly" in {
    val msg = mediator.expectMsgClass(classOf[RegisterForReceive])
    assert(msg.messageType == classOf[ClientManagerMessage])
    parentProbe.expectMsg(Ready(classOf[ClientManager]))
  }

  val fakeClient = TestProbe()
  def repBlock = {      // This block will be repeated you see
    parent.tell(new LogIn(user) with ClientManagerMessage, fakeClient.ref)
    mediator.expectMsg(new LogIn(user) with BatchProcessorMessage)
    mediator.expectMsg(GetLogs(user))
    mediator.reply(JsNull)
    fakeClient.expectMsg(Push(Json.obj("logs" -> JsNull)))
    mediator.expectMsg(GetUserDataSets(user))
    mediator.reply(JsNull)
    fakeClient.expectMsg(Push(Json.obj("data-sets" -> JsNull)))
    mediator.expectMsg(GetUserResults(user))
    //parentProbe.expectMsgClass(35 seconds, classOf[SysError])   // Too long to test, but works
    mediator.reply(JsNull)
    fakeClient.expectMsg(Push(Json.obj("results" -> JsNull)))
  }

  it should "Do the initial honors of notifying LogIn, " +
    "asking for Logs, Data-sets and Results" in repBlock

  import models.jsonWrites._
  val sLog = sampleLog(user)
  it should "send push message to correct user" in {
    parent ! MessageToClient(user, sLog)
    fakeClient.expectMsg(Push(Json.obj("log" -> Json.toJson(sLog))))
  }

  it should "not push messages to unavailable clients" in {
    parent ! MessageToClient("some1else", sLog)
    fakeClient.expectNoMsg()
  }

  it should "not push messages to clients who have logged out" in {
    parent.tell(new LogOut(user) with ClientManagerMessage, fakeClient.ref)
    parent ! MessageToClient(user, sLog)
    fakeClient.expectNoMsg()
    mediator.expectMsg(new LogOut(user) with BatchProcessorMessage)
  }

  it should "again do the initial work" in repBlock

  watch(fakeClient.ref)
  it should "kill / disconnect the clients when given a FinishWork directive" in {
    parent ! FinishWork
    expectMsgClass(classOf[Terminated])   // First the fakeClient terminates
    expectMsg(FinishedWork)               // Then we receive the FinishedWork meant
  }                                       // meant for the application manager
}
