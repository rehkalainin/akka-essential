package part3_Actors

import part3_Actors.ChangingActorBehavior.Mom.{Food, MomStart}
import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ChangingActorBehavior extends App {


  object Mom {
    case class Food(food: String)
    case class Ask(question: String)
    case class MomStart(ref: ActorRef)
    val VEGETABLES = "vegetables"
    val CHOCOLAT = "chokolat"
  }

  class Mom extends Actor {
    import Mom._
    import FussyKid._

    override def receive: Receive = {
      //test our integration
      case MomStart(kid)=>
        kid ! Food(VEGETABLES)
        kid ! Food(VEGETABLES)
        kid ! Food(CHOCOLAT)
        kid ! Food(CHOCOLAT)
        kid ! Food(VEGETABLES)
        kid ! Ask("Do you want play?")
      case KidSuccess => println("[mom actor] My kid is happy")
      case KidReject => println("[mom actor] My kid is sad")
    }
  }

  object FussyKid {
    case object KidSuccess
    case object KidReject
    val HAPPY = "happy"
    val SAD = "sad"
  }

  class FussyKid extends Actor {
    import FussyKid._
    import Mom._
    var state = HAPPY

    override def receive: Receive = {
      case Food(VEGETABLES) => state = SAD
      case Food(CHOCOLAT) => state = HAPPY
      case Ask(_) =>
        if (state == HAPPY) sender() ! KidSuccess
        else sender() ! KidReject
    }
  }

  class StatelesFussyKid extends Actor{
    import Mom._
    import FussyKid._

    override def receive: Receive = happyReceive

    def happyReceive: Receive ={
      case Food(VEGETABLES)=> context.become(sadReceive) // changing message handler (behavior actor)
      case Food(CHOCOLAT)=>context.unbecome() // continue working with this handler
      case Ask(_)=> sender() ! KidSuccess
    }
    def sadReceive:Receive= {
      case Food(VEGETABLES)=>context.unbecome()
      case Food(CHOCOLAT)=> context.become(happyReceive) // changing message handler (behavior actor)
      case Ask(_)=> sender() ! KidReject
    }
  }

  val actorSystem = ActorSystem("mySystem")
  val mom = actorSystem.actorOf(Props[Mom])
  val kid = actorSystem.actorOf(Props[FussyKid])
  val statelesFussyKid = actorSystem.actorOf(Props[StatelesFussyKid])

  mom ! MomStart(statelesFussyKid)
}
