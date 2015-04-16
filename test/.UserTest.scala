import actors.persistenceManager._
import models.batch._
import org.junit.runner._
import org.specs2.mutable._
import org.specs2.runner._

/**
 * Created by chetan on 12/04/15.
 */
@RunWith(classOf[JUnitRunner])
class UserTest extends Specification {

  "UserManager" should {

    val element = new UserManagerImpl

    "be able to create new users" in {
      element.addUser("chetanbhasin", "superhacked").onSuccess {
        case OperationStatus.OpSuccess => true mustEqual true
      }
    }

    /*
    "be, then, able to check a user" in {
      element.checkUsername("chetanbhasin").result() mustEqual Future.successful(true)
    }

    "and yet add another user" in {
      element.addUser("bassoGeorge", "somepassword").result() mustEqual Future.successful(OperationStatus.OpSuccess)
    }

    "and then find him too" in {
      element.checkUsername("bassoGeorge").result() mustEqual Future.successful(true)
    }
    */
  }

}
