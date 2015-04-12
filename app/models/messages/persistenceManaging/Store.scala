package models.messages.persistenceManaging

/**
 * Created by chetan on 12/04/15.
 */


/**
 * Message to store a particular dataset to the disk
 * @param username username who the dataset belongs to
 * @param datasetName name of the dataset
 * @param datasetPath path of the dataset
 * @param storageMethod higher order function for storing the dataset
 */
case class Store(username: String, datasetName: String, datasetPath: String, storageMethod: String => Unit)
