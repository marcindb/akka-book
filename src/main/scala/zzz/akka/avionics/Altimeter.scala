package zzz.akka.avionics

import akka.actor.{Props, Actor, ActorSystem, ActorLogging}
import scala.concurrent.duration._
import akka.actor.ActorRef
import akka.actor.OneForOneStrategy
import akka.actor.SupervisorStrategy._

object Altimeter {
	case class RateChange(amount: Float)
	case class AltitudeUpdate(altitude: Double)
	
	def apply() = new Altimeter with ProductionEventSource
}

trait AltimeterProvider {
  
  def newAltiemeter = new Altimeter with ProductionEventSource
    
}

class Altimeter extends Actor with ActorLogging {
  this: EventSource =>
    
  import Altimeter._

  implicit val ec = context.dispatcher

  val ceiling = 43000

  val maxRateOfClimb = 5000

  var rateOfClimb = 0f

  var altitude = 0.0

  var lastTick = System.currentTimeMillis

  val ticker = context.system.scheduler.schedule(100.millis, 100.millis, self, Tick)
  
  case class CalculateAltitude(lastTick: Long, tick: Long, roc: Double)
  
  case class AltitudeCalculated(newTick: Long, altitude: Double)
  
  override def supervisorStrategy = OneForOneStrategy(-1, Duration.Inf){
    case _ => Restart
  }
  
  val altitudeCalculator = context.actorOf(Props(
      new Actor{
        def receive = {
          case CalculateAltitude(lastTick, tick, roc) =>
            val alt = ((tick - lastTick) / 60000.0) * roc
            sender !  AltitudeCalculated(tick, alt)
        }
      }))
  
  case object Tick

  def altimeterReceive: Receive = {
  	case RateChange(amount) => 
  	   rateOfClimb = amount.min(1.0f).max(-1.0f) * maxRateOfClimb
  	   log.info(s"Altimeter changed rate of climb $rateOfClimb")
  	case Tick =>
  	   val tick = System.currentTimeMillis
  	   altitudeCalculator ! CalculateAltitude(lastTick, tick, rateOfClimb)
  	   lastTick = tick  	
  	case AltitudeCalculated(tick, altdelta) =>
  	   altitude += altdelta
  	   sendEvent(AltitudeUpdate(altitude))
  }

  def receive = eventSourceReceive orElse altimeterReceive

  override def postStop(): Unit = ticker.cancel

}