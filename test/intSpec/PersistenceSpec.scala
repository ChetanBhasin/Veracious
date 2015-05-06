package intSpec

import actors.persistenceManager.Persistence
import akka.actor.Props
import akka.pattern._
import models.messages.persistenceManaging.GetUserDataSets
import play.api.libs.json.JsNull

import scala.concurrent.Await
import scala.concurrent.duration._

/**
 * Created by basso on 06/05/15.
 */
class PersistenceSpec extends IntegrationTest {
  val user = "Anish"
  val persistence = system.actorOf(Props(classOf[Persistence], mediator), "testPersistence")
  implicit val timeout = akka.util.Timeout(20 seconds)

  "Persistence" should s"return data-sets (null) for $user" in {
    Await.result(persistence ? GetUserDataSets(user), timeout.duration) shouldBe JsNull
  }
}
