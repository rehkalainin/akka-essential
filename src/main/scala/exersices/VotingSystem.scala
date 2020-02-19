package exersices

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import exersices.VotingSystem.Citizen.{Vote, VoteStatusReply}
import exersices.VotingSystem.VotesAgregator.AgregateVotes

object VotingSystem extends App {

  object Citizen{
    case class Vote(candidate:String)
    case object VoteStatusRequest
    case class VoteStatusReply(candidate: Option[String])
  }
class Citizen extends Actor{
  import Citizen._
  var candidate :Option[String]= None
  override def receive: Receive = {
    case Vote(c)=> candidate=Some(c)
    case VoteStatusRequest=> sender() ! VoteStatusReply(candidate)
  }
}

  object VotesAgregator{
    case class AgregateVotes(citizens:Set[ActorRef])
  }
  class VotesAgregator extends Actor{
    import Citizen._
    var stillWaiting : Set[ActorRef]= Set()
    var currentStats: Map[String,Int]= Map()
    override def receive: Receive = {
      case AgregateVotes(citizens)=>
        stillWaiting = citizens
        citizens.foreach(citizenRef => citizenRef ! VoteStatusRequest)
      // a citizen hasn't vote yet
      case VoteStatusReply(None)=> sender() ! VoteStatusRequest
      case VoteStatusReply(Some(candidate))=>
        val newStillWaiting = stillWaiting - sender()
        val currentVotesOfCandidate: Int = currentStats.getOrElse(candidate,0)
        currentStats = currentStats + (candidate-> (currentVotesOfCandidate+1))

        if(newStillWaiting.isEmpty){
          println(s"[vote agregator] current stats : $currentStats")
        } else stillWaiting = newStillWaiting
      }
    }

  val system = ActorSystem("VotingSystem")

  val alis = system.actorOf(Props[Citizen])
  val bob = system.actorOf(Props[Citizen])
  val daniel = system.actorOf(Props[Citizen])
  val jim = system.actorOf(Props[Citizen])

  alis ! Vote("Martin")
  bob ! Vote("Jonas")
  daniel ! Vote("Roland")
  jim ! Vote ("Roland")

  val agregator = system.actorOf(Props[VotesAgregator])
  agregator ! AgregateVotes(Set(alis,bob,daniel,jim))
}
