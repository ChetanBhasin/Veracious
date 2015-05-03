package mocks

import actors.persistenceManager.UserManager
import models.batch.OperationStatus

import scala.collection.mutable.{Map => mMap}

/**
 * Created by basso on 25/04/15.
 */
class MockUserManager extends UserManager {
  val db = mMap(
    "Anish" -> "Blah",
    "Jibin" -> "blu"
  )

  def checkUsername(username: String) = db contains username
  def addUser(username: String, pass: String) = {
    db += ((username, pass))
    OperationStatus.OpSuccess
  }

  def removeUser(username: String) = {
    if (db contains username) {
      db -= username
      OperationStatus.OpSuccess
    } else OperationStatus.OpFailure
  }

  def changePassword(user: String, pass: String) = {
    if (db contains user) {
      db(user) = pass
      OperationStatus.OpSuccess
    } else OperationStatus.OpFailure
  }

  def authenticate(user: String, pass: String) =
    db.contains(user) && db(user) == pass
}
