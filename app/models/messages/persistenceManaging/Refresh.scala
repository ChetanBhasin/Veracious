package models.messages.persistenceManaging

/**
 * Created by chetan on 12/04/15.
 */


/**
 * Refreshes/updates the dataset from the source available
 * @param username username of the account the dataset belongs to
 * @param datasetName name of the dataset on the account
 */
case class Refresh(username: String, datasetName: String)
