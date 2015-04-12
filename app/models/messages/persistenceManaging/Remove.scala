package models.messages.persistenceManaging

/**
 * Created by chetan on 12/04/15.
 */


/**
 * Removes/deletes a dataset from the databse
 * @param username username of the account which the dataset belongs to
 * @param datasetName name of the dataset on the account
 */
case class Remove(username: String, datasetName: String)
