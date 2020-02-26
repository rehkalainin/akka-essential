package part6_Patterns

import akka.actor.{Actor, ActorLogging, ActorSystem, Props, Stash}

object StashDemo extends App {

  /**
   * goals stashes :
   *  - put message aside for later
   *  - when the time is right, prepend them to the mailbox and process them
   */

  /**
   * Resource actor:
   *  - open => it can receive read/write request to the resource
   *  - otherwise it will postpone all read / write requests until the state is open
   *
   * Resource actor is closed
   * -Open=> switch to the state
   * Read, Write mesage are POSTPONED
   *
   * Resource actor is open
   *  - Read / Write  and handled
   *  - Close => switch to the closed state
   *
   * [Open, Read, Read, Write]
   *  - switch to the open state
   *  - read the data
   *  - read the data
   *  - write the data
   *
   * [Read, Open, Write]
   *  - stash Read
   *  - Stash: [Read]
   * -open => switch to the open state
   * Mailbox: [Read, Write]
   *  - read, write, handle
   */
  case object Open

  case object Close

  case object Read

  case class Write(data: String)

  // step 1 - mix-in the Stash trait
  class ResourceActor extends Actor with ActorLogging with Stash {
    private var innerData = ""

    override def receive: Receive = closed

    def closed: Receive = {
      case Open =>
        log.info("Opening resource ...")
        context.become(open)
        // step 3 - unstashAll when you switch message handler
        unstashAll() // выполнить отложенные и гарантировано освободить stash
      case message =>
        log.info(s"Stashing $message because I can't handle it in close state")
        // step 2 - stash away what you can handle
        stash() // отложить
    }

    def open: Receive = {
      case Read =>
        log.info(s"I have read : $innerData")
      case Write(text) =>
        log.info(s"I have written : $text")
      case Close =>
        log.info("I have sweatched to close state")
        unstashAll()
        context.become(closed)
      case message =>
        log.info(s"Stashing $message because I can't handle in the open state")
        stash()
    }
  }

  val system = ActorSystem("ResourseSystem")
  val resourseActor = system.actorOf(Props[ResourceActor])

  resourseActor ! Read // stash
  resourseActor ! Open // opening // and pop Read from stash
  resourseActor ! Open //stash
  resourseActor ! Write("I love stash") // Write
  resourseActor ! Close // close // and pop Open from stash
  resourseActor ! Read // read
}
