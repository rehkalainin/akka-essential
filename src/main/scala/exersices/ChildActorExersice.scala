package exersices

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ChildActorExersice extends App {

  object WordCounterMaster{
    case class Initialize(nChildren:Int)
    case class WordCountTask(taskId:Int, text:String)
    case class WordCountReply(taskId:Int, counter:Int)
  }
  class WordCounterMaster extends Actor{
    import WordCounterMaster._

    override def receive: Receive = {
      case Initialize(n)=>
        println(s"[master] Initializing ... ")
        val childrenRefs = for (i<- 1 to n) yield context.actorOf(Props[WordCounterWorker],s"wcw_$i")
        context.become(withChildren(childrenRefs,0,0,Map()))
    }

    def withChildren(childrenRefs: Seq[ActorRef], currentChildrenIndex: Int, currentTaskId: Int, requestMap: Map[Int, ActorRef]):Receive={
      case text:String =>
        println(s"[master] I have receive : $text - I will send it to child $currentChildrenIndex")
        val originalSender = sender()
        val childRef = childrenRefs(currentChildrenIndex)
        childRef ! WordCountTask(currentTaskId,text)
        val nextChildIndex = (currentChildrenIndex+1)% childrenRefs.length
        val newTaskId = currentTaskId+1
        val newRequesMap = requestMap+ (currentTaskId-> originalSender)
        context.become(withChildren(childrenRefs,nextChildIndex,newTaskId,newRequesMap))

      case WordCountReply(id, counter)=>
        println(s"[master] I have received Reply for tast $id with $counter")
        val originalSender = requestMap(id)
        originalSender ! counter
        context.become(withChildren(childrenRefs,currentChildrenIndex,currentTaskId,requestMap-id))

    }
  }

  class WordCounterWorker extends Actor{
    import exersices.ChildActorExersice.WordCounterMaster._
    override def receive: Receive = {
      case WordCountTask(id,task)=> sender() ! WordCountReply(id, task.split(" ").length)
    }
  }

  class TestActor extends Actor{
    import WordCounterMaster._
    override def receive: Receive = {
      case "go"=>
        val master = context.actorOf(Props[WordCounterMaster],"master")
        master ! Initialize(3)
        val texts = List("I love akka", "Scala is awesome", "yes", "me too")
        texts.map(text=> master ! text)
      case count =>
        println(s"[test actor] I received a reply: $count")
    }
  }
  val system = ActorSystem("mySystem")
  val testActor = system.actorOf(Props[TestActor],"testActor")
  testActor ! "go"
}
