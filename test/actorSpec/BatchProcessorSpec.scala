package actorSpec

import _root_.mocks.{MockDsOp, MockMineOp, mockBatch1, mockBatch2}
import actors.batchProcessor.BatchProcessor
import actors.mediator.RegisterForReceive
import akka.actor.Props
import models.batch.Batch
import models.batch.OperationStatus._
import models.messages.application.{FinishWork, FinishedWork, Ready, SysError}
import models.messages.batchProcessing._
import models.messages.client.{LogIn, LogOut}
import models.messages.logger.Log

import scala.concurrent.duration._

class BatchProcessorSpec extends UnitTest {
  val user = "Anish"
  //val parent = system.actorOf(Props(classOf[BatchProcessor], mediator.ref), "testBatchProcessor")
  val parent = setupParent( Props(classOf[BatchProcessor], mediator.ref) )

  val submit = (b: Batch) => parent ! SubmitBatch(user, b)

  "Batch Processor" should "Setup correctly" in {
    val msg = mediator.expectMsgClass(classOf[RegisterForReceive])
    msg.messageType shouldBe classOf[BatchProcessorMessage]
    parentProbe.expectMsg(Ready(classOf[BatchProcessor]))
  }

  it should "send error message for orphan batch" in {
    submit(mockBatch1)
    parentProbe.expectMsgClass(classOf[SysError])
  }

  it should "send error message for orphan jobStatus" in {
    parent ! JobStatus(user, OpSuccess)
    parentProbe.expectMsgClass(classOf[SysError])
  }

  it should "correctly get a worker to start on a batch" in {
    parent ! new LogIn(user) with BatchProcessorMessage
    submit(mockBatch1)
    mediator.expectMsgClass(classOf[Log])
    mediator.expectMsg(SubmitMineJob(user, MockMineOp("Mn1")))
  }

  it should "sumbmit batch into queue and make worker start immediately on it after finished" in {
    submit(mockBatch2) // Should submit to queue
    parent ! JobStatus(user, OpSuccess) // For the first batch, it will finish now
    mediator.expectMsgClass(classOf[Log]) // after the first batch finishes
    mediator.expectMsgClass(classOf[Log])   // For the start of second batch
    mediator.expectMsg(SubmitDsOpJob(user, MockDsOp("Ds1"))) // For the second batch
    // Note, the second batch is still running
  }

  "The Worker" should "continue working on its batch even if the user logs out" in {
    parent ! LogOut(user)
    parent ! LogIn(user)
    parent ! JobStatus(user, OpSuccess)    // The second batch that is
    mediator.expectMsgClass(classOf[Log])
  }

  it should "die when it has no more batches to work on and the user has logged out" in {
    parent ! LogOut(user)
    submit(mockBatch1)
    parentProbe.expectMsgClass(classOf[SysError])
  }

  "Batch Processor" should "correctly handle the FinishWork directive" in {
    parent ! LogIn(user)
    submit(mockBatch1)      // Just one job
    mediator.expectMsgClass(classOf[Log])
    mediator.expectMsg(SubmitMineJob(user, MockMineOp("Mn1")))
    parent ! FinishWork     // Now it must wait for the batches to finish
    mediator.expectNoMsg(1 second)    // No msg just yet
    parent ! JobStatus(user, OpSuccess) // For the first batch, it will finish now
    mediator.expectMsgClass(classOf[Log]) // after the first batch finishes

    mediator.expectMsg(FinishedWork)
  }

}
