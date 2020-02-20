package part3_Actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ActorCapability extends App {

  case class SpecialMessage(msg: String)

  val system = ActorSystem("simpleActor")

  class SimpleActor extends Actor {
    override def receive: Receive = {
      case message: String => println(s"[simple actor] I have receive: $message")
      case number: Int => println(s"[sistem actor] I have receive: $number")
      case SpecialMessage(message) => println(s"[simple actor] I have receiver : $message")
      case SayToHi(ref) => ref ! "Hi" // REPLY and alice is being passed as sender
      case WireLessPhoneMessage(content, ref) => ref forward (content + s" ")
    }
  }

  // message can be any type
  // a) message must be IMMUTABLE
  // b) message must be serializeble
  // in practice use case class and case objects

  val simpleActor = system.actorOf(Props[SimpleActor], "simpleActor")
  simpleActor ! "Hello, actor"
  simpleActor ! 42
  simpleActor ! SpecialMessage("Some special")

  val alice = system.actorOf(Props[SimpleActor], "alice")
  val bob = system.actorOf(Props[SimpleActor], "bob")

  // actors can REPLY to message
  case class SayToHi(ref: ActorRef)

  alice ! SayToHi(bob)

  // forwarding message
  //D->A->B
  // forwarding == sending a message with the ORIGINAL sender
  case class WireLessPhoneMessage(content: String, ref: ActorRef)

  alice ! WireLessPhoneMessage("Hello", bob)
}
