package actors

import java.nio.file.{Files, Paths}

import play.api.libs.json.Json

import scala.io.Source

/**
 * The Miner sub-system. Works on the Mining jobs
 *
 * API:
 *  Incoming:
 *    SubmitMineJob (user: String, job: MineOp)
 *  Outgoing:
 *    JobStatus (user, OperationStatus)  =>> Directly back to the batchProcessor.Worker that sent the SubmitMineJob
 *    Log(...)
 *    Result (...) =>> Sent to the mediator,
 */
package object miner {

  private val myConfig = {
    lazy val confFilePath = Paths.get("./conf/minerConf.json")
    if (!Files.exists(confFilePath)) Files.createFile(confFilePath)

    val lines = Source.fromFile("./conf/psConf.json")
    val formatted = lines.getLines.mkString("\n")
    lines.close()

    formatted
  }

  // Todo: read this from configuration
  def getWorkerCount: Int = try {
    (Json.parse(myConfig) \ "workerCount").toString.toInt
  } catch {
    case _: Throwable => 5
  }

}
