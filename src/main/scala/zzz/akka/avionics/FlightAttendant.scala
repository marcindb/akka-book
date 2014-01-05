package zzz.akka.avionics

import scala.concurrent.duration._
import akka.actor.Actor


trait AttentandResponsiveness {
  val maxResponseTimeMS : Int
  def responseDuration = scala.util.Random.nextInt(maxResponseTimeMS).millis
}

object FlightAttendant {
  case class GetDrink(drinkname: String)
  case class Drink(drinkname: String)

  def apply() = new FlightAttendant with AttentandResponsiveness{
    val maxResponseTimeMS = 300000
  }
}

class FlightAttendant extends Actor {
  this: AttentandResponsiveness =>
  import FlightAttendant._
  
  implicit val ec = context.dispatcher
  
  def receive = {
    case GetDrink(drinkname) => context.system.scheduler.scheduleOnce(responseDuration, sender, Drink(drinkname))
  }

}