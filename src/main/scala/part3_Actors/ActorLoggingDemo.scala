package part3_Actors

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.event.Logging

object ActorLoggingDemo extends App{

  class SimpleActorWithExplicitLogging extends Actor{
    // 1 - explicit logging
    val logger = Logging(context.system, this)
    /*
    1. Debag
    2. Info
    3. Warning
    4. Errors
     */
    override def receive: Receive = {
      case message => logger.info(message.toString)
    }
  }
  val system = ActorSystem("mySystem")
  val simpleActor = system.actorOf(Props[SimpleActorWithExplicitLogging],"sympleLogerActor")
  simpleActor ! "Symple message logger "

  // 2 - ActorLogging

  class SimpleLogging extends Actor with ActorLogging{
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }
  val actorLog = system.actorOf(Props[SimpleLogging],"actorLoger")
  actorLog ! "Second symple logger"
}
