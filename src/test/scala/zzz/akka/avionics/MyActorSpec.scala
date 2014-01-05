package zzz.akka.avionics

import akka.actor.Actor
import akka.actor.ActorSystem
import akka.testkit.TestKit
import org.scalatest.WordSpecLike
import org.scalatest.Matchers
import org.scalatest.BeforeAndAfterAll
import org.scalatest.ParallelTestExecution
import akka.actor.Props

case object Ping
case object Pong

class MyActor extends Actor{
  
  override def receive: Receive = {
    case Ping => Pong
  }
}

class TestKitSpec(actorSystem: ActorSystem) extends TestKit(actorSystem)
with WordSpecLike
with Matchers
with BeforeAndAfterAll

class MyActorSpec extends TestKitSpec(ActorSystem("MyActorSpec")) {
  
  override def afterAll() {system.shutdown}
  
  def makeActor = system.actorOf(Props[MyActor],"MyActor")
  
  "My Actor" should {

    
    "respond with Pong to Ping" in {
      val a = makeActor
      a ! Ping
      expectMsg(Pong)
    }
  }

}

