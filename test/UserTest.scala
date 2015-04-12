import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import actors.persistenceManager._
import models.batch._

/**
 * Created by chetan on 12/04/15.
 */
@RunWith(classOf[JUnitRunner])
class UserTest extends Specification {

  "UserManager" should {

    "be able to create new users " in new UserManager {
      addUser("chetanbhasin", "superhacked") mustEqual OperationStatus.OpSuccess
    }

    "be, then, able to check a user" in new UserManager {
      checkUsername("chetanbhasin") mustEqual true
    }

    "and yet add another user" in new UserManager {
      addUser("bassoGeorge", "somepassword") mustEqual OperationStatus.OpSuccess
    }

    "and then find him too" in new UserManager {
      checkUsername("bassoGeorge") mustEqual true
    }
  }

}
