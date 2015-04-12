package actors

import actors.batchProcessor.BatchProcessor
import actors.mediator.RegisterForReceive
import actors.mocks._
import akka.actor.Props
import models.batch.Batch
import models.batch.OperationStatus._
import models.messages.batchProcessing._
import models.messages.client.{LogIn, LogOut}
import models.messages.logger.Log
import models.messages.{Ready, SysError}

class BatchProcessorSpec extends UnitTest {
  val user = "Anish"
  //val parent = system.actorOf(Props(classOf[BatchProcessor], mediator.ref), "testBatchProcessor")
  val parent = setupParent( Props(classOf[BatchProcessor], mediator.ref) )

  val submit = (b: Batch) => parent ! SubmitBatch(user, b)

  "Batch Processor" should "Setup correctly" in {
    mediator.expectMsgClass(classOf[RegisterForReceive])
    parentProbe.expectMsg(Ready("BatchProcessor"))
  }

  it should "send error message for orphan batch" in {
    submit(mockBatch2)
    mediator.expectMsgClass(classOf[SysError])
  }

  it should "send error message for orphan jobStatus" in {
    parent ! JobStatus(user, OpSuccess)
    mediator.expectMsgClass(classOf[SysError])
  }

  it should "correctly get a worker to start on a batch" in {
    parent ! LogIn(user)
    submit(mockBatch2)
    mediator.expectMsg(SubmitMineJob(user, MockMineOp("Mn1")))
  }

  it should "sumbmit batch into queue and make worker start immediately on it after finished" in {
    submit(mockBatch3) // Should submit to queue
    parent ! JobStatus(user, OpSuccess) // For the first batch, it will finish now
    mediator.expectMsgClass(classOf[Log]) // after the first batch finishes
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
    submit(mockBatch2)
    mediator.expectMsgClass(classOf[SysError])
  }
}
