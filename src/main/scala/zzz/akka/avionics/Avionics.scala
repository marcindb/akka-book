package zzz.akka.avionics

import akka.actor.{Props, Actor, ActorRef, ActorSystem}
import akka.pattern.ask
import scala.concurrent.Await
import akka.util.Timeout
import scala.concurrent.duration._

import scala.concurrent.ExecutionContext.Implicits.global

object Avionics {
	implicit val timeout = Timeout(5.seconds)

	val system = ActorSystem("Avionics")
	val plane = system.actorOf(Props(new Plane with AltimeterProvider 
	    with PilotProvider with LeadFlightAttendantProvider with HeadingIndicatorProvider), "Plane")

	def main(args: Array[String]) {
	    import Plane._
		val control = Await.result((plane ? Plane.GiveMeControl).mapTo[Controls], 5.seconds).controls
		
		system.scheduler.scheduleOnce(200.millis){
			control ! ControlSurfaces.StickBack(1f)
		}

		system.scheduler.scheduleOnce(1.seconds){
			control ! ControlSurfaces.StickBack(0f)
		}

		system.scheduler.scheduleOnce(3.seconds){
			control ! ControlSurfaces.StickBack(0.5f)
		}

		system.scheduler.scheduleOnce(4.seconds){
			control ! ControlSurfaces.StickBack(0f)
		}

		system.scheduler.scheduleOnce(5.seconds){
			system.shutdown()
		}
	} 
}