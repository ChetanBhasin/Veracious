package actors

import actors.mocks._
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit._
import org.scalatest._

abstract class UnitTest(_system: ActorSystem) extends TestKit(_system)
  with ImplicitSender with FlatSpecLike with Matchers with BeforeAndAfterAll {

  def this() = this(ActorSystem("test"))

  /** Setup the mediator */
  /*
  val mediator = system.actorOf(Props[Mediator], "mediator")
  mediator ! RegisterBroadcastMessage (classOf[GlobalBroadcast])
  */

  val mediator = TestProbe()
  val parentProbe = TestProbe()

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

  def setupParent (childProps: Props): ActorRef = {
    system.actorOf(Props(classOf[TestParent], childProps, parentProbe.ref), "testParent")
  }
}
