package zzz.akka.avionics

import org.scalatest.Matchers
import org.scalatest.WordSpecLike
import com.typesafe.config.ConfigFactory
import akka.actor.Actor
import akka.actor.ActorSystem
import akka.testkit.ImplicitSender
import akka.testkit.TestKit
import akka.testkit.TestProbe
import akka.actor.ActorRef
import scala.concurrent.duration._
import akka.pattern.ask
import akka.actor.Props
import scala.concurrent.Await
import akka.util.Timeout
import akka.actor.PoisonPill

class FakePilot extends Actor {
  override def receive = {
    case _ =>
  }
}

object PilotSpec {
  val copilotName = "Mary"
  val pilotName = "Mark"
  val configStr = s"""
    zzz.akka.avionics.flightcrew.copilotName = "$copilotName"
    zzz.akka.avionics.flightcrew.pilotName = "$pilotName""""
}

class PilotSpec extends TestKit(ActorSystem("PilotSpec", ConfigFactory.parseString(PilotSpec.configStr)))
  with ImplicitSender
  with WordSpecLike
  with Matchers {
  
  import PilotSpec._
  import Plane._
  
  def nilActor: ActorRef = TestProbe().ref
  
  val pilotPath = s"/user/TestPilots/$pilotName"
  val copilotPath = s"/user/TestPilots/$copilotName"
  
  def pilotsReadyToGo(): ActorRef = {
    val a = system.actorOf(Props(new IsolatedStopSupervisor with OneForOneStrategyFactory{
      def childStarter() {
        context.actorOf(Props[FakePilot], pilotName)
        context.actorOf(Props(new Copilot(testActor, nilActor, nilActor)), copilotName)
      }
    }), "TestPilots")
    implicit val askTimeout = Timeout(4.seconds)
    Await.result(a ? IsolatedLifeCycleSupervisor.WaitForStart, 3.seconds)
    system.actorFor(copilotPath) ! Pilot.ReadyToGo
    a
  }

  "Copilot" should {
    "take control when the Pilot dies" in {
      pilotsReadyToGo()
      system.actorFor(pilotPath) ! PoisonPill
      expectMsg(GiveMeControl)
      lastSender should be (system.actorFor(copilotPath))
    }
  }

}