package models.mining

import models.batch.job.Job
import models.messages.persistenceManaging.PersistenceMessage
import models.mining.Algorithm.Algorithm

/**
 * Created by basso on 08/04/15.
 *
 * Marker trait signifying the result of a mining operation,
 * required by the results management subsystem and the miner sub-system
 *
 * Chetan, please extend this trait to use any kind of result that is needed
 */

case class MinerResult(al: Algorithm, user: String, name: String, save: (String) => Unit, job: Job) extends PersistenceMessage
