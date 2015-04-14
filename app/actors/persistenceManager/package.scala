package actors

import java.nio.file.{Files, Paths}

import play.api.libs.json.Json

import scala.io.Source

/**
 * The persistence manager subsystem is responsible for directly performing disk operations
 * such as reading writing datasets to the disk, loading and refreshing of datasets from URLs
 * checking for existing users and modifying their records, modifying meta-store etc.
 */
package object persistenceManager {

  val myConfig = {
    lazy val confFilePath = Paths.get("./conf/psConf.json")
    if (!Files.exists(confFilePath)) Files.createFile(confFilePath)

    val lines = Source.fromFile("./conf/psConf.json")
    val formatted = lines.getLines.mkString("\n")
    lines.close()

    val out = Json.parse(formatted)
    out
  }

  def getChildActors: Int = (myConfig \ "DsChildOps").toString.toInt

}
