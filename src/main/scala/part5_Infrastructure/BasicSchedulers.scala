package part5_Infrastructure

import akka.actor.{Actor, ActorLogging, ActorSystem, Cancellable, Props}

import scala.concurrent.duration._

object BasicSchedulers extends  App {

  class SimpleActor extends Actor with ActorLogging{
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }
  val system = ActorSystem("SimpleActorSystem")
  val sympleActor = system.actorOf(Props[SimpleActor])

  system.log.info("Scheduleling reminder for simpleActor")

  implicit val dispatcher = system.dispatcher

  system.scheduler.scheduleOnce(1 second){ // one receive after delay = 1 second
  sympleActor ! "reminder"
  }

  val routine: Cancellable = system.scheduler.schedule(1 second, 2 second){ // every 2 second receive msg
    sympleActor ! "heartbeat"
  }
  system.scheduler.scheduleOnce(10 second){ // after 6 second routine will cancel
    routine.cancel()
  }




}
