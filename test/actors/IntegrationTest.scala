package actors

import actors.Mediator._
import akka.actor.{ActorSystem, Props}
import akka.testkit._
import models.messages.GlobalBroadcast
import org.scalatest._

abstract class IntegrationTest(_system: ActorSystem) extends TestKit(_system)
  with ImplicitSender with FlatSpecLike with Matchers with BeforeAndAfterAll {

  def this() = this(ActorSystem("test"))

  /** Setup the mediator */
  val mediator = system.actorOf(Props[Mediator])
  mediator ! RegisterBroadcastMessage (classOf[GlobalBroadcast])

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }
}
