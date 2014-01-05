package zzz.akka.avionics

import akka.actor.{Props, Actor, ActorLogging}
import akka.actor.ActorRef

object Plane {
	case object GiveMeControl
	case class Controls(controls: ActorRef)
}

class Plane extends Actor with ActorLogging {
	import Altimeter._
	import Plane._
	
	val cfgstr = "zzz.akka.avionics.flightcrew"
	val config = context.system.settings.config

	val altimeter = context.actorOf(Props(Altimeter()), "Altimeter")
	val controls = context.actorOf(Props(new ControlSurfaces(altimeter)),"ControlSurfaces")
	
	val pilot = context.actorOf(Props[Pilot], config.getString(s"$cfgstr.pilotName"))
	val copilot = context.actorOf(Props[Copilot], config.getString(s"$cfgstr.copilotName"))
	//val autopilot = context.actorOf(Props[Autopilot], "Autopilot")
	val flightAttendant = context.actorOf(Props(LeadFlightAttendant()), config.getString(s"$cfgstr.leadAttendantName"))
	

	def receive = {
		case GiveMeControl =>
		    log info("Plane giving control.")
		    sender ! Controls(controls)
		case AltitudeUpdate(altitude) =>
		    log info(s"Altitude is now : $altitude")
	}

    import EventSource._
	override def preStart() {
		altimeter ! RegisterListener(self)
		List(pilot, copilot) foreach{_ ! Pilot.ReadyToGo}
	}
}
