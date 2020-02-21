package part5_Infrastructure

import akka.actor.{Actor, ActorLogging, ActorSystem, Props, Timers}

import scala.concurrent.duration._

object BasicTimers extends App {

  case object TimerKey
  case object Start
  case object Stop
  case object Reminder

 class TimerHeartbeatActor extends Actor with ActorLogging with Timers{
  // timers.startSingleTimer(TimerKey, Start, 1 second) // запуск Start черeз 1 second после запуска timers
   // остановка таймера осуществяется с помощью планировщика system.schedule
   override def receive: Receive = {
     case Start =>
       log.info("Bootstraping ")
       timers.startPeriodicTimer(TimerKey, Reminder, 2 second)
     case Reminder=>
       log.info("I am alive")
     case Stop =>
       log.info("Stopping")
       timers.cancel(TimerKey)
       context.stop(self)
   }
 }
  val system = ActorSystem("TimerActorSystem")
  val timerHeartbeatActor = system.actorOf(Props[TimerHeartbeatActor],"timerActor")
  implicit val dispatcher = system.dispatcher

  system.scheduler.scheduleOnce(500 millis ){ // start after 500 millis delay
    timerHeartbeatActor ! Start
  }
  system.scheduler.scheduleOnce(10 second){ // stop after 10 second delay
    timerHeartbeatActor ! Stop
  }

}
