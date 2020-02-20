package part3_Actors

import akka.actor.{Actor, ActorSystem, Props}

object ActorIntro extends App {

  val actorSystem = ActorSystem("firstActorSystem")
  println(actorSystem.name)

  // part2 - create actors
  // word counter actor
  class WordCounter extends Actor {
    var totalWord = 0

    // behavor
    override def receive: Receive = { // Receive == PartialFunction[Any, Unit]
      case message: String => println(s"[wordCounter] I have received:  $message")
        totalWord += message.split(" ").length
      case msg => println(s" I can't understand ${msg.toString}")
    }
  }

  // part 3 - instantiate actor
  val wordCounter = actorSystem.actorOf(Props[WordCounter], "wordCounter") // actors incapsulated and instantiate throw fabric method .actorOf
  val anotherWordCounter = actorSystem.actorOf(Props[WordCounter], "AnotherWordCounter") // important different names
  // part 4 - communicate !
  wordCounter ! "I am learning Akka and it's pretty good"
  anotherWordCounter ! "A different message"
  // async
}
