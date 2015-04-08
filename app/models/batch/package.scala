package models

/**
 * Created by basso on 07/04/15.
 *
 * This package will contain all the classes related to creation and
 * management of the user submitted batches.
 *
 * The package contains:
 *  1. The definition of the Batch class
 *  2. The sub-package job which contains the Job hierarchy
 *  3. The Batch factory which will create the actual Batch object
 *  4. The OperationStatus enumeration
 *
 * Maintained by Anish
 */
package object batch {

  /** OperationStatus
    * A Job or Batch may finish execution with one of the following status.
    * This is needed for logging perposes
    */
  object OperationStatus extends Enumeration {
    type OperationStatus = Value
    val OpSuccess, OpFailure, OpWarning = Value
  }
}

// TODO: Create the batch class
