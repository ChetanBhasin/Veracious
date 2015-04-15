package actors

import actors.batchProcessor.BatchProcessor
import actors.mediator.RegisterForReceive
import actors.mocks._
import akka.actor.Props
import akka.testkit.TestProbe
import models.batch.Batch
import models.batch.OperationStatus._
import models.messages.batchProcessing._
import models.messages.client.{LogIn, LogOut}
import models.messages.logger.Log
import models.messages.{Ready, SysError}

class BatchProcessorSpec extends IntegrationTest {
  val user = "Anish"
  val mockMediator = TestProbe()
  val testProcessor = system.actorOf(Props(classOf[BatchProcessor], mockMediator.ref), "testBatchProcessor")

  val submit = (b: Batch) => testProcessor ! SubmitBatch(user, b)

  "Batch Processor" should "Setup correctly" in {
    mockMediator.expectMsg(RegisterForReceive(testProcessor, classOf[BatchProcessorMessage]))
    mockMediator.expectMsg(Ready("BatchProcessor"))
  }

  it should "send error message for orphan batch" in {         // TODO: have to work on this one
    submit(mockBatch2)
    mockMediator.expectMsgClass(classOf[SysError])
  }

  it should "send error message for orphan jobStatus" in {
    testProcessor ! JobStatus(user, OpSuccess)
    mockMediator.expectMsgClass(classOf[SysError])
  }

  it should "correctly get a worker to start on a batch" in {
    testProcessor ! LogIn(user)
    submit(mockBatch2)
    mockMediator.expectMsg(SubmitMineJob(user, MockMineOp("Mn1")))
  }

  it should "sumbmit batch into queue and make worker start immediately on it after finished" in {
    submit(mockBatch3) // Should submit to queue
    testProcessor ! JobStatus(user, OpSuccess) // For the first batch, it will finish now
    mockMediator.expectMsgClass(classOf[Log]) // after the first batch finishes
    mockMediator.expectMsg(SubmitDsOpJob(user, MockDsOp("Ds1"))) // For the second batch
    // Note, the second batch is still running
  }

  "The Worker" should "continue working on its batch even if the user logs out" in {
    testProcessor ! LogOut(user)
    testProcessor ! LogIn(user)
    testProcessor ! JobStatus(user, OpSuccess)    // The second batch that is
    mockMediator.expectMsgClass(classOf[Log])
  }

  it should "die when it has no more batches to work on and the user has logged out" in {
    testProcessor ! LogOut(user)
    submit(mockBatch2)
    mockMediator.expectMsgClass(classOf[SysError])
  }
}
