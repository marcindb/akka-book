package zzz.akka.avionics

import akka.testkit.TestKit
import akka.actor.ActorSystem
import akka.testkit.ImplicitSender
import org.scalatest.WordSpecLike
import org.scalatest.BeforeAndAfterAll
import org.scalatest.Matchers
import akka.testkit.TestLatch
import akka.actor.Actor
import akka.testkit.TestActorRef
import akka.actor.Props
import scala.concurrent.Await
import scala.concurrent.duration._


class AltimeterSpec extends TestKit(ActorSystem("AltimeterSpec"))
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  import Altimeter._
  import EventSource._

  override def afterAll = system.shutdown

  class Helper {
    object EventSourceSpy {
      val latch = TestLatch(1)
    }
    trait EventSourceSpy extends EventSource {
      override def sendEvent[T](event: T): Unit = EventSourceSpy.latch.countDown
      override def eventSourceReceive = Actor.emptyBehavior
    }
    def slicedAltimeter = new Altimeter with EventSourceSpy

    def actor = {
      val a = TestActorRef[Altimeter](Props(slicedAltimeter))
      (a, a.underlyingActor)
    }
  }

  "Altimeter" should {
    "record rate of climb changes" in new Helper {
      val (_, real) = actor
      real.receive(RateChange(1.0f))
      real.rateOfClimb should be(real.maxRateOfClimb)
    }
    "keep rate changes within bounds" in new Helper {
      val (_, real) = actor
      real.receive(RateChange(2.0f))
      real.rateOfClimb should be(real.maxRateOfClimb)
    }
    "calculate altitude changes" in new Helper {
      val ref = system.actorOf(Props(Altimeter()))
      ref ! RegisterListener(testActor)
      ref ! RateChange(1f)
      fishForMessage(){
        case AltitudeUpdate(altitude) if altitude == 0 => false
        case AltitudeUpdate(altitude) => true
      }
    }
    "send events" in new Helper {
      val (_, real) = actor
      Await.ready(EventSourceSpy.latch, 1.seconds)
      EventSourceSpy.latch.isOpen should be (true)
    }
  }

}