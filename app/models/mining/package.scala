package models

/**
 * Created by basso on 07/04/15.
 */
package object mining {

  /** An enumeration of all the mlops we use **/
  object Algorithm extends Enumeration {
    type Algorithm = Value
    val Clustering, FPM, SVM, ALS = Value
  }
}

/** Notes from Anish,
  * Scrap the oldAlgo
  *
  * Define each algo in it's own object.
  * Single function which takes the required parameters and gives uniform result,
  * preferably an scala.util.Either[ErrorString, Result]
  */
