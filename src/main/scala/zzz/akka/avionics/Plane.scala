package zzz.akka.avionics

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import Altimeter.AltitudeUpdate
import EventSource.RegisterListener
import IsolatedLifeCycleSupervisor.WaitForStart
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.pattern.ask
import akka.util.Timeout

object Plane {
  case object GiveMeControl
  case class Controls(controls: ActorRef)
}

class Plane extends Actor with ActorLogging {

  this: AltimeterProvider with PilotProvider with LeadFlightAttendantProvider =>

  import Altimeter._
  import Plane._

  val cfgstr = "zzz.akka.avionics.flightcrew"
  val config = context.system.settings.config

  val pilotName = config.getString(s"$cfgstr.pilotName")
  val copilotName = config.getString(s"$cfgstr.copilotName")
  val attendantName = config.getString(s"$cfgstr.leadAttendantName")


  def actorForControls(name: String) = context.actorFor("Equipment/" + name)
  
  def actorForPilots(name: String) = context.actorFor("Pilots/" + name)
  
  override def preStart(){
    import EventSource.RegisterListener
    import Pilot.ReadyToGo
    startEquipment()
    startPeople()
    actorForControls("Altimeter") ! RegisterListener(self)
    actorForPilots(pilotName) ! ReadyToGo
    actorForPilots(copilotName) ! ReadyToGo
  }
  
  implicit val askTimeout = Timeout(1.second)
  def startEquipment() {
    val controls = context.actorOf(Props(new IsolatedResumeSupervisor with OneForOneStrategyFactory {
      def childStarter() {
        val alt = context.actorOf(Props(newAltiemeter), "Altimeter")
        context.actorOf(Props(newAutopilot), "Autopilot")
        context.actorOf(Props(new ControlSurfaces(alt)), "ControlSurfaces")
      }
    }), "Equipment")
    import IsolatedLifeCycleSupervisor._
    Await.result(controls ? WaitForStart, 1.second)
  }

  def startPeople() {
    val plane = self
    val controls = actorForControls("ControlSurfaces")
    val autopilot = actorForControls("Autopilot")
    val altimeter = actorForControls("Altimeter")
    val people = context.actorOf(Props(new IsolatedStopSupervisor with OneForOneStrategyFactory {
      def childStarter() {
        context.actorOf(Props(newPilot(plane, autopilot, controls, altimeter)), pilotName)
        context.actorOf(Props(newCopilot(plane, autopilot, altimeter)), copilotName)
      }
    }), "Pilots")
    context.actorOf(Props(newLeadFlightAttendant), attendantName)
    import IsolatedLifeCycleSupervisor._
    Await.result(people ? WaitForStart, 1.second)
  }

  def receive = {
    case GiveMeControl =>
      log info ("Plane giving control.")
      sender ! Controls(actorForControls("ControlSurfaces"))
    case AltitudeUpdate(altitude) =>
      log info (s"Altitude is now : $altitude")
  }

}
