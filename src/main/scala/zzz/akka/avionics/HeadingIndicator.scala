package zzz.akka.avionics

import akka.actor.ActorLogging
import akka.actor.Actor
import scala.concurrent.duration._

trait HeadingIndicatorProvider {
  
  def newHeadingIndicator = new HeadingIndicator with ProductionEventSource
    
}

object HeadingIndicator {
  case class BankChange(amount: Float)
  case class HeadingUpdate(headingFloat: Float)
}

trait HeadingIndicator extends Actor with ActorLogging{
  this: EventSource =>
  
  import HeadingIndicator._
  import context._
  
  case object Tick
  
  val maxDegPerSec = 5
  
  val ticker = system.scheduler.schedule(100.millis, 100.millis, self, Tick)
  
  var lastTick: Long = System.currentTimeMillis()
  
  var rateOfBank = 0f
  
  var heading = 0f
  
  def headingIndiatorReceive: Receive = {
    case BankChange(amount) =>
      rateOfBank = amount.min(1.0f).max(-1.0f)
    case Tick =>
      val tick = System.currentTimeMillis()
      val timeDelta = (tick - lastTick) / 1000f
      val degs = rateOfBank * maxDegPerSec
      heading = (heading + (360 + (timeDelta * degs))) % 360
      lastTick = tick
      sendEvent(HeadingUpdate(heading))
  }
  
  def receive = eventSourceReceive orElse headingIndiatorReceive
  
  override def postStop(): Unit = ticker.cancel

}