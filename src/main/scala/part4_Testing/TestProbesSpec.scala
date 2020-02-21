package part4_Testing

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}
import part4_Testing.TestProbesSpec.{Master, Register, RegistrationAct, Report, SlaveWork, Work, WorkCompleted}

class TestProbesSpec extends TestKit(ActorSystem("TestProbesSpecSystem"))
  with ImplicitSender
  with WordSpecLike
  with BeforeAndAfterAll{

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "Test Prob Spec " should {
    "register slave" in {
      val master = system.actorOf(Props[Master])
      val slave = TestProbe("slave") // типа замоканый
      master ! Register(slave.ref)
      expectMsg(RegistrationAct)
    }
  }
  "send the work to the slave actor" in {
    val master = system.actorOf(Props[Master])
    val slave = TestProbe("slave")
    master ! Register(slave.ref)
    expectMsg(RegistrationAct) // testActor expected RegistrationAct

    val text = "I love akka"
    master ! Work(text)
    // the interaction between master actor and slave actor
    slave.expectMsg(SlaveWork(text, testActor)) // testActor - original requester
    slave.reply(WorkCompleted(3, testActor))

    expectMsg(Report(3)) // testActor expected Report
  }
  "aggregate data correctly" in {
    val master = system.actorOf(Props[Master])
    val slave = TestProbe("slave")
    master ! Register(slave.ref)
    expectMsg(RegistrationAct) // testActor expected RegistrationAct

    val text = "I love akka"
    master ! Work(text)
    master ! Work(text)
    master ! Work(text)

    slave.receiveWhile(){
      case SlaveWork(`text`, `testActor`)=> slave.reply(WorkCompleted(3,testActor))
    }

    expectMsg(Report(3))
    expectMsg(Report(6))
    expectMsg(Report(9))
  }

}

object TestProbesSpec{
  // scenario
  /*
    word counteng actor hierarchy master-slave

    send some work to the master
    - master send to slave some work
    - slave processes some work and replies to master
    - master aggregate the result
    master send total result to original requester

   */
    case class Register(slaveRef: ActorRef)
    case class Work(text:String)
    case class SlaveWork(text:String, originalRequester:ActorRef)
    case class WorkCompleted(totalWordCounter:Int, originalRequester:ActorRef)
    case class Report(totalCounter:Int)
    case object RegistrationAct

  class Master extends Actor{
    override def receive: Receive = {
      case Register(slaveRef)=>
        sender() ! RegistrationAct
        context.become(online(slaveRef, 0))
      case _=> // ignore
    }
    def online(slaveRef:ActorRef, totalWordCounter:Int):Receive={
      case Work(text)=> slaveRef ! SlaveWork(text, sender())
      case WorkCompleted(counter, originalRequester)=>
        val newTotalCounter = totalWordCounter +counter
        originalRequester ! Report(newTotalCounter)
        context.become(online(slaveRef,newTotalCounter))
    }
  }
  class Slave extends Actor{
    override def receive: Receive = {
      case SlaveWork(text, sender)=>
        val counter = text.split(" ").length
        sender ! WorkCompleted(counter, sender)
      case _ =>
    }
  }

}

