package actors

import java.io.{File, PrintWriter}

import actors.client.Client.props
import actors.logger._
import actors.mediator.RegisterForReceive
import akka.actor.Props
import models.batch.OperationStatus
import models.batch.job.DsAddFromUrl
import models.messages.Ready
import models.messages.client.LogIn
import models.messages.logger._
import models.mining.Algorithm
import play.api.libs.json._

import scala.concurrent.duration._
import scala.io.Source

/**
 * Created by basso on 10/04/15.
 */
class LoggerSpec extends IntegrationTest {
  val user = "Anish"
  val logFile = "./test/resources/logFile_test"

  /** Setup the mediator */
  //mediator ! RegisterForNotification(testActor)           // THIS was the problem!!!
  mediator ! RegisterForReceive(testActor, classOf[Ready])


  val logger = system.actorOf(Props(classOf[Logger], logFile, mediator), "logger")
  "Logger" should "notify that it is ready" in {
    expectMsg(4 seconds, Ready("Logger"))
  }


  /** Setup the logFile to original form */
  val writer = new PrintWriter(new File(logFile))
  val orig = Source.fromFile("./test/resources/logFile_test_orig").getLines().toList
  orig.foreach {writer.println}
  writer.close()
  /** ---------------------------------- */

  /*
    TODO: Problems were being created here because the logger wasn't initializing on time. It is imperative
    that a feedback mechanism be made to make sure all the sub-systems are initialised and ready
   */

  val client = system.actorOf(props(mediator)(user, testActor), "testClient2")
  "Client" should "Notify that it has logged in" in {
    expectMsg(4 seconds, LogIn(user))
  }

  var msg: JsValue = JsNull
  "Logger" should "send the client a log message on startup" in {
    msg = expectMsgClass(5 seconds, classOf[JsValue])
    msg \ "log" != JsNull
  }

  it should "send correct number of logs" in {
    (msg \ "log").asOpt[List[JsObject]] match {
      case None => fail("Log object failure")
      case Some(lst) =>
        if (lst.length != 5) fail("Some logs from other users have been supplied")
    }
  }

  val sampleLog = Log(
    OperationStatus.OpSuccess,
    user,
    "The operation was a success",
    DsAddFromUrl("data-setName","desc", Algorithm.Clustering ,"https://blah.com/ds"))

  it should "send the log that belongs to the client" in {
    logger ! sampleLog
    expectMsgClass(classOf[JsValue])
  }

  it should "write the log to file" in {
    if (Source.fromFile(logFile).getLines().length != orig.length + 1) fail()
  }

  it should "not send the log that belongs to some other client" in {
    logger ! sampleLog.copy(user = "OtherUser")
  }
}
