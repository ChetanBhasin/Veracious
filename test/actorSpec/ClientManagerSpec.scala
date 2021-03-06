package actorSpec

import _root_.mocks.sampleLog
import actors.client.ClientManager
import actors.mediator.RegisterForReceive
import actors.persistenceManager.GetDsData
import akka.actor.{Props, Terminated}
import akka.testkit.TestProbe
import models.messages.application.{FinishWork, FinishedWork, Ready}
import models.messages.batchProcessing.BatchProcessorMessage
import models.messages.client._
import models.messages.logger.GetLogs
import models.messages.persistenceManaging.GetUserDataSets
import play.api.libs.json._

/**
 * Created by basso on 22/04/15.
 */
class ClientManagerSpec extends UnitTest {
  val user = "Anish"
  val parent = setupParent( Props(classOf[ClientManager], mediator.ref))

  "Client Manager" should "Setup correctly" in {
    val msg = mediator.expectMsgClass(classOf[RegisterForReceive])
    msg.messageType shouldBe classOf[ClientManagerMessage]
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
    fakeClient.expectMsg(Push(Json.obj("datasets" -> JsNull)))
  }

  it should "Do the initial honors of notifying LogIn, " +
    "asking for Logs, Data-sets and Results" in repBlock

  import models.jsonWrites._
  val sLog = sampleLog(user)
  it should "send push message to correct user and ask for update" in {
    parent ! MessageToClient(user, sLog)
    mediator.expectMsg(GetUserDataSets(user))
    fakeClient.expectMsg(Push(Json.obj("log" -> Json.toJson(sLog))))
    mediator.reply(JsNull)
    fakeClient.expectMsg(Push(Json.obj("datasets" -> JsNull)))
  }

  it should "tell us that the client is already logged into the system" in {
    parent ! UserAlreadyLoggedIn(user)
    expectMsg(true)
  }

  it should "ask persistence for result when required" in {
    parent ! AskForResult(user, "dsName")
    mediator.expectMsg(GetDsData(user, "dsName"))
  }

  it should "push the result to client on receiving it" in {
    mediator.reply(JsNull)
    fakeClient.expectMsg(Push(Json.obj("result" -> JsNull)))
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
