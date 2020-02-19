package exersices

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import exersices.BankAccountActor.BankAccount.{Deposit, Statement, Withdraw}
import exersices.BankAccountActor.Person.LifeCycleAccount

object BankAccountActor extends App {

  /*
      a Bank account as actor
      receives
      - Deposit an amount
      - Withdraw an amount
      - Statement

      replies with
      -Success
      -Failure

      interact with some other kind of actor
   */
object BankAccount{
    case class Deposit(amount:Int)
    case class Withdraw(amount:Int)
    case object Statement

    case class TransactionSuccess(message:String)
    case class TransactionFailure(reson:String)
  }

class BankAccount extends Actor{
  import BankAccount._
  var funds = 0
  override def receive: Receive = {
    case Deposit(amount)=>
      if(amount<=0) sender() ! TransactionFailure(s"[bank account] Invalid amount")
      else {
        funds += amount
        sender() ! TransactionSuccess(s"[bank account] Funds has deposited on: $amount")
      }
    case Withdraw(amount)=>
      if (amount>funds | amount<0) sender() ! TransactionFailure(s"[bank account] Invalid amounte")
      else {
        funds -= amount
        sender() ! TransactionSuccess(s"[bank account] Funds has withdraw on : $amount")
      }
    case Statement => println(s"[bank account] Balance account is: $funds")
  }
}
  object Person{
    case class LifeCycleAccount(account: ActorRef)
  }
  class Person extends Actor{
    override def receive: Receive = {
      case LifeCycleAccount(account)=> {
        account ! Deposit(10000)
        account ! Withdraw(90000)
        account ! Withdraw(500)
        account ! Statement
      }
      case message => println(message)
    }
  }
  val actorSystem = ActorSystem("bankAccounteSystem")
  val account = actorSystem.actorOf(Props[BankAccount],"accountActor")
  val person = actorSystem.actorOf(Props[Person], "clientBank")

  person ! LifeCycleAccount(account)
}
