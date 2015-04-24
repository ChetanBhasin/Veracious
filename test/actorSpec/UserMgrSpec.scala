package actorSpec

/**
 * Created by basso on 24/04/15.
 */
import actors.persistenceManager.UserManagerImpl
import models.batch.OperationStatus

class UserMgrSpec extends UnitTest {
  val userMgr = UserManagerImpl(system)
  val userA = "Anish"
  val passA = "blahBlah"
  val passB = "bluBlu"

  "User Manager" should s"Declare that $userA does not exist" in {
    userMgr.checkUsername(userA) == false
  }

  it should s"Add new user $userA" in {
    userMgr.addUser(userA, passA) == OperationStatus.OpSuccess
  }

  it should s"Find $userA in record now" in {
    userMgr.checkUsername(userA)
  }

  it should s"Authenticate $userA" in {
    userMgr.authenticate(userA, passA) == true
  }

  it should s"Not authenticate $userA with password $passB" in {
    userMgr.authenticate(userA, passB) == false
  }

  it should s"Change password from $passA to $passB for $userA" in {
    userMgr.changePassword(userA, passB) == OperationStatus.OpSuccess
  }

  it should s"Now authenticate $userA with new password $passB" in {
    userMgr.authenticate(userA, passB) == true
  }

  it should s"Remove user $userA on request" in {
    userMgr.removeUser(userA) == OperationStatus.OpSuccess
  }

  it should s"Fail to authenticate $userA since it does not exist now" in {
    userMgr.authenticate(userA, passB) == false
  }
}
