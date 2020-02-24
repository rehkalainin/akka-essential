package part5_Infrastructure

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.routing.{Broadcast, FromConfig, RoundRobinGroup}
import com.typesafe.config.ConfigFactory

object RoutersGroup extends App {

    /*
       3. - router with actors created everyWhere
       GROUP router
     */

  // 3.1  - in code

    class Slave extends Actor with ActorLogging{
      override def receive: Receive = {
        case message => log.info(message.toString)
      }
    }
  val config = ConfigFactory.load().getConfig("routersDemo")
  val system = ActorSystem("GroupeRouter", config)
  val slaveList = (1 to 5).map(i=>system.actorOf(Props[Slave],s"slave_$i")).toList
  val slavePath = slaveList.map(slave=> slave.path.toString)

  val groupMaster = system.actorOf(RoundRobinGroup(slavePath).props())

//    for(i <- 1 to 10) {
//      groupMaster ! s"[$i] Hello world "
//    }

  // 3.1  - from config
  val groupMaster2 = system.actorOf(FromConfig.props(), "groupMaster2")
  for(i <- 1 to 10) {
    groupMaster2 ! s"[$i] Hello world "
  }

    /*
         special messages
     */
   groupMaster2 ! Broadcast("Hello, once")
  // PoisonPill and Kill are NOT routed
  // AddRoutee, Remove, Get handled only by the routing actor
}
