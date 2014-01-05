package zzz.akka.avionics

import akka.actor.Actor
import akka.actor.ActorRef

object Pilot {
  case object ReadyToGo
  case object RealiquishControl
}

class Pilot extends Actor {

  import Pilot._
  import Plane._

  var controls: ActorRef = context.system.deadLetters
  var copilot: ActorRef = context.system.deadLetters
  var autopilot: ActorRef = context.system.deadLetters
  var copilotName = context.system.settings.config.getString("zzz.akka.avionics.flightcrew.copilotName")
  def receive = {
    case ReadyToGo =>
      context.parent ! GiveMeControl
      copilot = context.actorFor("../" + copilotName)
      autopilot = context.actorFor("../Autopilot")
    case Controls(controlSurfaces) => controls = controlSurfaces
  }

}

class Copilot extends Actor {

  import Pilot._

  var controls: ActorRef = context.system.deadLetters
  var copilot: ActorRef = context.system.deadLetters
  var autopilot: ActorRef = context.system.deadLetters
  var pilotName = context.system.settings.config.getString("zzz.akka.avionics.flightcrew.pilotName")
  def receive = {
    case ReadyToGo =>
      copilot = context.actorFor("../" + pilotName)
      autopilot = context.actorFor("../Autopilot")
  }

}