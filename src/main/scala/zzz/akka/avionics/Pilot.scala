package zzz.akka.avionics

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Terminated

object Pilot {
  case object ReadyToGo
  case object RealiquishControl
}

trait PilotProvider {
  
  def newPilot(plane: ActorRef, autopilot: ActorRef, controls: ActorRef, altimeter: ActorRef): Actor = new Pilot(plane, autopilot,controls, altimeter)
  def newCopilot(plane: ActorRef, autopilot: ActorRef, altimeter: ActorRef): Actor = new Copilot(plane, autopilot, altimeter)
  def newAutopilot = new Autopilot

}

class Pilot(plane: ActorRef, autopilot: ActorRef, var controls: ActorRef, altimeter: ActorRef) extends Actor {

  import Pilot._
  import Plane._
  
  var copilot: ActorRef = context.system.deadLetters
  var copilotName = context.system.settings.config.getString("zzz.akka.avionics.flightcrew.copilotName")

  def receive = {
    case ReadyToGo =>
      copilot = context.actorFor("../" + copilotName)
      plane ! GiveMeControl
    case Controls(controlSurfaces) => controls = controlSurfaces
  }

}

class Copilot(plane: ActorRef, autopilot: ActorRef, altimeter: ActorRef) extends Actor {

  import Pilot._
  import Plane._

  var controls: ActorRef = context.system.deadLetters
  var pilot: ActorRef = context.system.deadLetters  
  var pilotName = context.system.settings.config.getString("zzz.akka.avionics.flightcrew.pilotName")
  
  def receive = {
    case ReadyToGo => 
      pilot = context.actorFor("../" + pilotName)
      context.watch(pilot)
    case Terminated(_) =>
      plane ! GiveMeControl
    case Controls(controlSurfaces) => controls = controlSurfaces
  }

}

class Autopilot extends Actor {
  
  def receive = Actor.emptyBehavior
}