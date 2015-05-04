package actorSpec

/**
 * Created by basso on 24/04/15.
 */

import actors.persistenceManager.UserManagerImpl
import models.batch.OperationStatus

class UserMgrSpec extends UnitTest {

  val userMgr = UserManagerImpl(system, mediator.ref)
  val userA = "Anish"
  val passA = "blahBlah"
  val passB = "bluBlu"

  "User Manager" should s"Declare that $userA does not exist" in {
    userMgr.checkUsername(userA) shouldBe false
  }

  it should s"Add new user $userA" in {
    userMgr.addUser(userA, passA) shouldBe OperationStatus.OpSuccess
  }

  it should s"Find $userA in record now" in {
    userMgr.checkUsername(userA) shouldBe true
  }

  it should s"Authenticate $userA" in {
    userMgr.authenticate(userA, passA) shouldBe true
  }

  it should s"Not authenticate $userA with password $passB" in {
    userMgr.authenticate(userA, passB) shouldBe false
  }

  it should s"Change password from $passA to $passB for $userA" in {
    userMgr.changePassword(userA, passB) shouldBe OperationStatus.OpSuccess
  }

  it should s"Now authenticate $userA with new password $passB" in {
    userMgr.authenticate(userA, passB) shouldBe true
  }

  it should s"Remove user $userA on request" in {
    userMgr.removeUser(userA) shouldBe OperationStatus.OpSuccess
  }

  it should s"Fail to authenticate $userA since it does not exist now" in {
    userMgr.authenticate(userA, passB) shouldBe false
  }
}
