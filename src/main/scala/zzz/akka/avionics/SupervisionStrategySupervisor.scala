package zzz.akka.avionics

import scala.concurrent.duration.Duration
import akka.actor.SupervisorStrategy._
import akka.actor.SupervisorStrategy
import akka.actor.OneForOneStrategy
import akka.actor.AllForOneStrategy

trait SupervisionStrategySupervisor {
  
  def makeStrategy(maxNrRetries: Int, withinTimeRange: Duration)(decider: Decider): SupervisorStrategy

}

trait OneForOneStrategyFactory extends SupervisionStrategySupervisor{
  
  def makeStrategy(maxNrRetries: Int, withinTimeRange: Duration)(decider: Decider): SupervisorStrategy =
    OneForOneStrategy(maxNrRetries, withinTimeRange)(decider)
  
}

trait AllForOneStrategyFactory extends SupervisionStrategySupervisor{
  
  def makeStrategy(maxNrRetries: Int, withinTimeRange: Duration)(decider: Decider): SupervisorStrategy =
    AllForOneStrategy(maxNrRetries, withinTimeRange)(decider)
  
}