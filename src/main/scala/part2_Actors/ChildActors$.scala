package part2_Actors

import part2_Actors.ChildActors.Parent.{CreateActor, TellChild}
import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ChildActors extends App{

// Actor can crate other actor
  object Parent{
  case class CreateActor(name:String)
  case class TellChild(message:String)
}
  class Parent extends Actor{
    override def receive: Receive = {
      case CreateActor(name)=>
        println(s"${self.path} Create actor $name")
        val childRef = context.actorOf(Props[Child], s"$name")
        context.become(withChild(childRef))
    }
    def withChild(childRef: ActorRef):Receive={
      case TellChild(message)=> childRef ! message
    }
  }


  class Child extends Actor{
    override def receive: Receive = {
      case message=> println(s"${self.path} I got message : $message")
    }
  }
  val system = ActorSystem("ChildActorSystem")
  val parent = system.actorOf(Props[Parent],"parent")

  parent ! CreateActor("myActorChild")
  parent ! TellChild("Hello my child actor")

}
