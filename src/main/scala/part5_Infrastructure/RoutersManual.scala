package part5_Infrastructure

import akka.actor.{Actor, ActorLogging, ActorSystem, Props, Terminated}
import akka.routing.{ActorRefRoutee, RoundRobinRoutingLogic, Router}

object RoutersManual extends App {

  /*
      1. Manual Router - rare used
   */
  class Master extends Actor{
    // 1. create routees
    // 5 actor routees based off slave actors
    private val slaves = for(i <-1 to 5) yield{
      val slave = context.actorOf(Props[Slave],s"slave_$i")
      context.watch(slave) // отслеживание каждого slave
      ActorRefRoutee(slave) // помечаем каждого slave для подальшего роутинга
    }
    private val router = Router(RoundRobinRoutingLogic(),slaves)
    // 2. define router
    override def receive: Receive = {
      // 4. handle the termination/ lifecicle of the routees
      case Terminated(ref)=> // если один из акторов погибает (становиться нерабочим)
        router.removeRoutee(ref) // выводим его из роутинга
        val newSlave = context.actorOf(Props[Slave])
        context.watch(newSlave) // ствим его не контроль "мерти"
        router.addRoutee(newSlave) // добавляем в роутинг нового

      // 3. route massages
      case message =>
        router.route(message,sender())
    }
  }


  class Slave extends Actor with ActorLogging{
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }
  val system = ActorSystem("RoutingDemo" /* TODO*/)
  val master = system.actorOf(Props[Master])
  for (i<- 1 to 10){ // без yield можно писать если тип вывода Unit
    master ! s"[$i]Hello world message"
  }

}
