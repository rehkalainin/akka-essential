package exersices

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import exersices.VotingSystem.Aggregator.AggregateVotes
import exersices.VotingSystem.Citizen.Vote

object VotingSystem extends App {

  object Citizen {
    case class Vote(candidate: String)
    case object VoteStatusRequest
    case class VoteStatusReply(candidate: Option[String])
  }

  class Citizen extends Actor {
    import Citizen._

    override def receive: Receive = {
      case Vote(candidate) => context.become(voted(candidate))
      case VoteStatusRequest => sender() ! VoteStatusReply(None)
    }
    def voted(candidate: String): Receive = {
      case VoteStatusRequest => sender() ! VoteStatusReply(Some(candidate))
    }
  }

  object Aggregator {
    case class AggregateVotes(citizens: Set[ActorRef])
  }

  class Aggregator extends Actor {

    import Citizen._

    override def receive: Receive = {

      case AggregateVotes(citizens) =>
        citizens.map(citizenRef => citizenRef ! VoteStatusRequest)
        context.become(awaitingStatuses(citizens, Map()))
    }

    def awaitingStatuses(stillWaiting: Set[ActorRef], currentStats: Map[String, Int]): Receive = {
      case VoteStatusReply(None) => sender() ! VoteStatusRequest
      case VoteStatusReply(Some(candidate)) =>
        val newStillWaiting = stillWaiting - sender()
        val currentVotesOfCandidate = currentStats.getOrElse(candidate, 0)
        val newStats = currentStats + (candidate -> (currentVotesOfCandidate + 1))
        if (newStillWaiting.isEmpty) {
          println(s"[agregator] current stats : $newStats")
        } else {
          context.become(awaitingStatuses(newStillWaiting, newStats))
        }
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
    jim ! Vote("Roland")

    val agregator: ActorRef = system.actorOf(Props[Aggregator])
    agregator ! AggregateVotes(Set(alis, bob, daniel, jim))
}
