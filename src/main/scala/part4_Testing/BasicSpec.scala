package part4_Testing

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

import scala.collection.immutable
import scala.concurrent.duration._
import scala.util.Random

class BasicSpec extends TestKit(ActorSystem("BasicSpec"))
 with ImplicitSender
  with WordSpecLike
  with BeforeAndAfterAll {

  // setup
  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  import BasicSpec._

    "A Simple actor" should {
    "send back the same message " in {
      val echoActor = system.actorOf(Props[SimpleActor])
      val message = "Test message"
      echoActor ! message

      expectMsg(message) // akka.test.single-expect-default
    }
    "Black hole actor" should{
      "send back the same message" in {
        val blackHole = system.actorOf(Props[BlackHole])
        val message = "Test message"
        blackHole ! message

        expectNoMessage(1.second)
      }
    }
    "A Lab Test Actor" should{
      val labTestActor = system.actorOf(Props[LabTestActor])
      "reply turned message to upper case" in{
        val message = "I love akka"
        labTestActor ! message
        expectMsg("I LOVE AKKA")
      }
      "reply to a greeting" in {
        labTestActor ! "greeting"
        expectMsgAnyOf("hi","hello") // one of the
      }
      "reply with favorite tech " in{
        labTestActor ! "favoriteTech"
        expectMsgAllOf("Scala", "Akka") // all
      }

      "reply one of the cool tech" in {
        labTestActor ! "favoriteTech"
        expectMsgPF(){
          case "Scala"=>
          case "Akka"=>
          case "Java"=>
        }
      }
    }
  }

}

  object BasicSpec {
    class SimpleActor extends Actor{
      override def receive: Receive = {
        case message => sender() ! message
      }
    }
    class BlackHole extends Actor{
      override def receive: Receive = Actor.emptyBehavior
    }
    class LabTestActor extends Actor{
      val random = new Random()
      override def receive: Receive ={
        case "favoriteTech"=>
          sender() ! "Akka"
          sender() ! "Scala"
        case "greeting"=>
          if (random.nextBoolean()) sender() ! "hi" else sender() ! "hello"
        case message:String=> sender() ! message.toUpperCase
      }
    }

  }





