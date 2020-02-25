package part5_Infrastructure

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.Random

object Dispatchers extends App {

  class Counter extends Actor with ActorLogging {
    var counter = 0

    override def receive: Receive = {
      case message=>
        counter += 1
        log.info(s"[$counter] $message")
    }
  }
  //val config = ConfigFactory.load().getConfig("dispatchersDemo")
  val system = ActorSystem("DispatcherSystem")
  val actor = for(i <- 1 to 10 ) yield {
    system.actorOf(Props[Counter].withDispatcher("my-dispatcher"),s"counter_$i")}
//  val random = new Random()
//  for (i <- 1 to 1000){
//    actor(random.nextInt(10)) ! i
//  }

  // method 2 - from config

  val rtjvmActor = system.actorOf(Props[Counter], "rtjvm")

  /**
   *  Dispatcher implement the ExecutionContext trait
   */

  class DBActor extends Actor with ActorLogging {
    implicit val executionContext = context.system.dispatchers.lookup("my-dispatcher") // context.dispatcher в этом случе поток будет блокировать оба актора
    // context.system.dispatchers.lookup("my-dispatcher") - сначала отработает неблокирующий поток, пото блокирующий)
    override def receive: Receive = {
      case message => Future{
        Thread.sleep(5000)
        log.info(s"$message")
      }
    }
  }
  val dbActor = system.actorOf(Props[DBActor])
  val nonBlockingActor = system.actorOf(Props[Counter])
 // dbActor ! "Meaning of life is 42"
  for(i<- 1 to 200) {
    dbActor ! s"[$i] Blocking Important message"
    nonBlockingActor ! s"[$i] NonBlocking Important message"
  }


}
