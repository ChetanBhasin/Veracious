package actorSpec

import java.io.{File, PrintWriter}

import actorSpec.mocks._
import actors.logger._
import actors.mediator.RegisterForReceive
import akka.actor.Props
import models.messages.Ready
import models.messages.client.MessageToClient
import models.messages.logger._
import play.api.libs.json._

import scala.concurrent.duration._
import scala.io.Source

/**
 * Created by basso on 10/04/15.
 */
class LoggerSpec extends UnitTest {
  val user = "Anish"
  implicit val logFile = "./test/resources/logFile_test"

  /** Setup the logFile to original form */
  val writer = new PrintWriter(new File(logFile))
  val orig = Source.fromFile("./test/resources/logFile_test_orig").getLines().toList
  orig.foreach {writer.println}
  writer.close()
  /** ---------------------------------- */

  val parent = setupParent (Props(classOf[Logger], mediator.ref, logFile))
  // The parentProbe will receive messages

  "Logger" should "register itself at the mediator" in {
    mediator.expectMsgClass(classOf[RegisterForReceive])
  }

  it should "notify that it is ready" in {
    parentProbe.expectMsg(5 seconds, Ready(classOf[Logger]))
    //mediator.expectMsg(Ready("Logger"))
  }

  var msg: JsValue = JsNull
  it should "send the clientManager the list of logs when asked" in {
    parent ! GetLogs(user)
    msg = expectMsgClass(classOf[JsValue])
    assert (msg != JsNull)
    /*parent ! LogIn(user)    // Simulating Client Login
    val fMsg = expectMsgClass(classOf[MessageToClient])
    assert (fMsg.username == user)
    msg = fMsg.msg
    msg \ "log" != JsNull*/
  }

  it should "send correct number of logs" in {
    (msg).asOpt[List[JsObject]] match {
      case None => fail("Log object failure")
      case Some(lst) =>
        if (lst.length != 5) fail("Some logs from other users have been supplied")
    }
  }

  it should "send the log that belongs to the client" in {
    parent ! sampleLog(user)
    val msg = mediator.expectMsgClass(classOf[MessageToClient])
    assert (msg.username == user)
  }

  it should "write the log to file" in {
    if (Source.fromFile(logFile).getLines().length != orig.length + 1) fail()
  }
}
