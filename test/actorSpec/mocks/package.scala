package actorSpec

import java.time.LocalDateTime

import models.batch.job.{DataSetOp, DsAddFromUrl, MineOp}
import models.batch.{Batch, OperationStatus}
import models.messages.logger.Log
import models.mining.Algorithm

/**
 * Created by basso on 11/04/15.
 */
package object mocks {
  case class MockDsOp(id: String) extends DataSetOp {
    val logWrite = id
    def setId(a:String) = this
  }

  case class MockMineOp(id: String) extends MineOp {
    val logWrite = id
    def setId(a:String) = this
  }

  val mockBatch = Batch("mockBatch", LocalDateTime.now(),
    jobs = List(
      MockDsOp("Ds1"), MockMineOp("Mn1"),
      MockDsOp("Ds2"), MockMineOp("Mn2")
    ))

  val mockBatch1 = mockBatch.copy(jobs =
    List( MockMineOp("Mn1") ))

  val mockBatch2 = mockBatch.copy(jobs =
    List( MockDsOp("Ds1") ))

  def sampleLog(user: String) = Log(
    OperationStatus.OpSuccess,
    user,
    "The operation was a success",
    DsAddFromUrl("data-setName","desc", Algorithm.Clustering ,"https://blah.com/ds"))
}
