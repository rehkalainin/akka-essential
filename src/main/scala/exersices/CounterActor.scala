package exersices

import akka.actor.{Actor, ActorSystem, Props}
import exersices.CounterActor.Counter.{Decrement, Increment, Print}

/*
      a Counter actor
      - increment
      - decrement
      - print
 */

object CounterActor extends App {

  val system = ActorSystem("CounterSystem")

  class Counter extends Actor {
    import Counter._
    var counter = 0
    override def receive: Receive = {
      case Increment => counter += 1
      case Decrement => counter -= 1
      case Print => println(s"[counter] Current count $counter")
    }
  }

  object Counter {
    case object Increment
    case object Decrement
    case object Print
  }

  val counter = system.actorOf(Props[Counter], "myCounter")

  (1 to 5).map(_ => counter ! Increment)
  (1 to 3).map(_ => counter ! Decrement)
  counter ! Print
}
