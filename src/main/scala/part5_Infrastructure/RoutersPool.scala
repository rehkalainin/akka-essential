package part5_Infrastructure

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.routing.{FromConfig, RoundRobinPool}
import com.typesafe.config.ConfigFactory

object RoutersPool extends App{
  /*
      2. Router actor with his own children
          POOL router
   */

  // 2.1 Programmatically (in code)

  class Slave extends Actor with ActorLogging{
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }

  // val system = ActorSystem("PoolDemo")
  // val poolMaster = system.actorOf(RoundRobinPool(5).props(Props[Slave]),"simplePoolMaster")
  //  for(i<- 1 to 10 ){
  //    poolMaster! s"[$i] Hello world"


  // 2.2 from configuration

  val config = ConfigFactory.load().getConfig("routersDemo")
  val system = ActorSystem("PoolDemo2", config)

  val poolMaster2 = system.actorOf(FromConfig.props(Props[Slave]), "poolMaster2")
  for(i<- 1 to 10 ){
    poolMaster2 ! s"[$i] Hello world"
  }
  }



