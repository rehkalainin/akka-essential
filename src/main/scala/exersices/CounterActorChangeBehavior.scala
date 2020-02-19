package exersices

import akka.actor.{Actor, ActorSystem, Props}
import exersices.CounterActorChangeBehavior.Counter.{Decrement, Increment, Print}

object CounterActorChangeBehavior extends App {

  object Counter {
    case object Increment
    case object Decrement
    case object Print
  }

  class Counter extends Actor {
    import Counter._
    override def receive: Receive =counterReceiver(0)
      def counterReceiver(counter:Int):Receive={
        case Increment=>{
          println(s"[counter] Increment counter: $counter")
          context.become(counterReceiver(counter+1)) // благодаря contex сохраняется значение counter, become изменяет поведение
        }
        case Decrement=>{
          println(s"[counter] decrement counter: $counter")
          context.become(counterReceiver(counter-1)) // // благодаря contex сохраняется значение counter, become изменяет поведение
        }
        case Print=> println(s"[counter] Current counter: $counter")
      }
  }

  val system = ActorSystem("mySystem")
  val counterActor = system.actorOf(Props[Counter],"counter")

  (1 to 5).map(_=> counterActor ! Increment )
  (1 to 3).map(_=> counterActor ! Decrement )
  counterActor ! Print
}
