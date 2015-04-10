package actors

import actors.batchProcessor._
import actors.mocks._
import akka.actor.Props
import akka.testkit.TestProbe
import models.batch.OperationStatus._
import models.messages.batchProcessing._
import models.messages.logger.Log

class BatchWorkerSpec extends IntegrationTest {
  val user = "Anish"

  val mockMediator = TestProbe()    // Will intercept messages to mediator
  val parentProbe = TestProbe()     // Will give the messages to the parent

  val mockParent = system.actorOf(Props(classOf[MockWorkerParent], user, mockMediator.ref, parentProbe.ref))

  "Worker" should "notify the parent that it is available" in {
    parentProbe.expectMsg(IAmFree(user))
  }

  it should "accept the batch and submit the first job" in {
    mockParent ! mockBatch
    mockMediator.expectMsg(SubmitDsOpJob(user, MockDsOp("Ds1")))
  }

  it should "log a batch failure and become available when a job is unsuccessful" in {
    mockParent ! OpFailure
    val log = mockMediator.expectMsgClass(classOf[Log])
    assert(log.status == OpFailure)
    parentProbe.expectMsg(IAmFree(user))
  }

  it should "accept batch & submit mine job" in {
    mockParent ! mockBatch2
    mockMediator.expectMsg(SubmitMineJob(user, MockMineOp("Mn1")))
  }

  it should "log a batch success and become available when the batch finishes" in {
    mockParent ! OpSuccess
    val log = mockMediator.expectMsgClass(classOf[Log])
    assert(log.status == OpSuccess)
    parentProbe.expectMsg(IAmFree(user))
  }

}
