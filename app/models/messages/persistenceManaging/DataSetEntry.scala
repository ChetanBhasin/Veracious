package models.messages.persistenceManaging

import models.messages.client.PushData
import models.mining.Algorithm.Algorithm

/**
 * Created by chetan on 14/04/15.
 */

case class DataSetEntry(name: String, desc: String, datatype: String, targetAlgorithm: Algorithm, status: String, source: String) extends PushData
