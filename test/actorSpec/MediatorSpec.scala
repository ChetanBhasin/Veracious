package actorSpec

import actors.mediator._
import akka.actor.ActorRef
import akka.testkit.TestProbe

import scala.concurrent.duration._

class MediatorSpec extends IntegrationTest {

  object GlobalMsg
  object TargetMsg
  object OtherMsg
  val probe = TestProbe()
  val probe2 = TestProbe()

  val receiveReg = mediator ! RegisterForReceive (_: ActorRef, TargetMsg.getClass)
  val unregister = mediator ! Unregister (_: ActorRef)
  val notifyReg = mediator ! RegisterForNotification(_: ActorRef)

  "Mediator" should "forward our message" in {
    receiveReg (probe.ref)
    mediator ! TargetMsg
    probe.expectMsg(TargetMsg)
  }

  it should "not send us back our message" in {
    receiveReg (testActor)
    mediator ! TargetMsg
    probe.expectMsg(TargetMsg)
    expectNoMsg(1 second)
    unregister (testActor)
  }

  it should "not send us messages not meant for us" in {
    mediator ! OtherMsg
    probe.expectNoMsg(1 second)
  }

  it should "broadcast global messages to all (target receivers and watchers) except the sender" in {
    mediator ! RegisterBroadcastMessage(GlobalMsg.getClass)
    notifyReg(testActor)
    notifyReg(probe2.ref)
    mediator ! GlobalMsg
    probe.expectMsg(GlobalMsg)
    probe2.expectMsg(GlobalMsg)
    expectNoMsg(1 second)
  }

  it should "not send any more messages after unregistering a target receiver" in {
    unregister(probe.ref)
    mediator ! TargetMsg
    probe.expectNoMsg(2 seconds)
  }

  it should "not send global messages after unregistering notification receiver" in {
    unregister(probe2.ref)
    mediator ! GlobalMsg
    probe2.expectNoMsg(2 seconds)
  }

  it should "send broadcast messages but no personal messages to actor registered for notification" in {
    notifyReg(probe.ref)
    mediator ! TargetMsg
    probe.expectNoMsg()
  }
}
