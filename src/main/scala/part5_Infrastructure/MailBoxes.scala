package part5_Infrastructure

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.dispatch.{PriorityGenerator, UnboundedPriorityMailbox}
import com.typesafe.config.Config

object MailBoxes extends App {
  val system = ActorSystem("MailBoxSystem")

  class SimpleActor extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(s"$message")
    }
  }

  /**
   *  1. interesting case - custom priority mailbox
   * P0 -> most important
   * P1->
   * P2->
   * P3->
   */

  // step 1 - mailbox definition
  class SupportTicketPriorityMailbox(settings: ActorSystem.Settings, config: Config)
    extends UnboundedPriorityMailbox(
      PriorityGenerator {
        case message: String if message.startsWith("[P0]") => 0
        case message: String if message.startsWith("[P1]") => 1
        case message: String if message.startsWith("[P2]") => 2
        case message: String if message.startsWith("[P3]") => 3
        case _ => 4
      })

  // step 2 -  make it known in the config
  // step 3 - attach the dispatcher to this actor
  val supportTicketLogger = system.actorOf(Props[SimpleActor].withDispatcher("support-ticket-dispatcher"))
  supportTicketLogger ! "[P3] this thing would be nice to have"
  supportTicketLogger ! "[P0] this need to be solve NOW"
  supportTicketLogger ! "[P1] do this when you have the time"

}
