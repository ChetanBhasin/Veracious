package models.mining

import models.mining.Algorithm.Algorithm

/**
 * Created by basso on 08/04/15.
 *
 * Marker trait signifying the result of a mining operation,
 * required by the results management subsystem and the miner sub-system
 *
 * Chetan, please extend this trait to use any kind of result that is needed
 */

case class MinerResult(al: Algorithm, save: (String) => Unit)
