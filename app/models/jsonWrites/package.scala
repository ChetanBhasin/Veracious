package models

import models.messages.logger.Log
import models.messages.persistenceManaging.DataSetEntry
import play.api.libs.json._

/**
 * Created by basso on 22/04/15.
 *
 * Writes converters for the push types in the application
 */
package object jsonWrites {
  implicit val logWrites = new Writes[Log] {
    def writes(log: Log) = Json.obj(
      "status" -> log.status.toString.substring(2).toUpperCase,
      "message" -> log.msg,
      "activity" -> log.content.logWrite
    )
  }

  implicit val dataSetEntryWrites = new Writes[DataSetEntry] {
    def writes(dsEntry: DataSetEntry) = Json.obj(
      "name" -> dsEntry.name,
      "type" -> dsEntry.datatype,
      "algo" -> dsEntry.targetAlgorithm,
      "status" -> dsEntry.status,
      "source" -> dsEntry.source
    )
  }

  //TODO: implement a miner results
}
