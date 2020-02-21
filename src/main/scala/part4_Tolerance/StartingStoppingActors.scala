package part4_Tolerance

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Kill, PoisonPill, Props}
import part4_Tolerance.StartingStoppingActors.Parent.{StartChild, Stop, StopChild}

object StartingStoppingActors extends App{

  val system = ActorSystem("StartingStoppingSystem")

  object Parent{
    case class StartChild(name:String)
    case class StopChild(name:String)
    case object Stop
  }
  class Parent extends Actor with ActorLogging {
    import Parent._
    override def receive: Receive = withChildren(Map())
    def withChildren(children: Map[String, ActorRef]): Receive={
      case StartChild(name)=>
        log.info(s"Starting child:  $name")
        val child = context.actorOf(Props[Child],name)
        context.become(withChildren(children+(name->child)))
      case StopChild(name)=>
        log.info(s"Stopping child:  $name")
        val childOption = children.get(name)
        childOption.map(childRef=> context.stop(childRef))
      case Stop=>
        log.info("Stopping parent")
        context.stop(self)
      case message=>
        log.info(message.toString)
    }
  }
  class Child extends Actor with ActorLogging{
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }

  /**
   * 1 - using context.stop
   */

//  val parent = system.actorOf(Props[Parent],"parent")
//  parent ! StartChild("child1")
//  val child = system.actorSelection("user/parent/child1")
//  child ! "Hi kid!"
//  parent ! StopChild("child1")
//  parent ! StartChild("child2")
//  val child2 = system.actorSelection("user/parent/child2")
//  child2 ! "child2 is alive"
//  parent ! Stop  // после остановки parent останавливаются также все дети
//
//  (1 to 100).map(i=> child2 ! s"[$i] Second child still alive")
//  (1 to 50).map(i=> parent ! s"[$i]  Parent still alive")

  /**
   *  2- using special messages
   */
  val looseActor = system.actorOf(Props[Child])
  looseActor ! "hello, looseActor"
  looseActor ! PoisonPill // убивает актор в логах INFO (спец команда)
  looseActor ! " looseActor are you alive?"

  val abruptlyTerminated = system.actorOf(Props[Child])
  abruptlyTerminated ! "hello, abruptlyTerminated"
  abruptlyTerminated ! Kill // убивает актор и в логах кидает ERROR (спец команда)
  abruptlyTerminated ! " abruptlyTerminated are you alive?"
}
