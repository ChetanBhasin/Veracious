package actorSpec

import _root_.mocks.{MockDsOp, MockMineOp, mockBatch, mockBatch1}
import actors.batchProcessor._
import akka.actor.Props
import models.batch.OperationStatus._
import models.messages.batchProcessing._
import models.messages.logger.Log

class BatchWorkerSpec extends UnitTest {
  val user = "Anish"

  val parent = setupParent(Props(classOf[Worker], user, mediator.ref))

  it should "accept the batch and submit the first job" in {
    parent ! mockBatch
    mediator.expectMsgClass(classOf[Log])
    mediator.expectMsg(SubmitDsOpJob(user, MockDsOp("Ds1")))
  }

  it should "log a batch failure and become available when a job is unsuccessful" in {
    parent ! OpFailure
    val log = mediator.expectMsgClass(classOf[Log])
    log.status shouldBe OpFailure
    parentProbe.expectMsg(IAmFree(user))
  }

  it should "accept batch & submit mine job" in {
    parent ! mockBatch1
    mediator.expectMsgClass(classOf[Log])
    mediator.expectMsg(SubmitMineJob(user, MockMineOp("Mn1")))
  }

  it should "log a batch success and become available when the batch finishes" in {
    parent ! OpSuccess
    val log = mediator.expectMsgClass(classOf[Log])
    log.status shouldBe OpSuccess
    parentProbe.expectMsg(IAmFree(user))
  }

}
