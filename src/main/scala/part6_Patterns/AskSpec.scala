package part6_Patterns

import akka.actor.AbstractActor.Receive
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}
import part6_Patterns.AskSpec.KVActor

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success}
// step 1 - import akka ask pattern
import akka.pattern.ask // We use ask when we expect single response
import akka.pattern.pipe

class AskSpec extends TestKit(ActorSystem("AskSpec"))
  with ImplicitSender
  with WordSpecLike
  with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  import AskSpec._
  import AuthManager._

  "An authenticator" should {
    authenticatorTestSuite(Props[AuthManager])
  }
  "Pipe authenticator manager" should {
    authenticatorTestSuite(Props[PipeAuthManager])
  }

  def authenticatorTestSuite(props: Props) = {
    import AuthManager._
    "fail authenticate a non-reister user" in {
      val authManager = system.actorOf(Props[PipeAuthManager])
      authManager ! Authenticate("Kostya", "akka")
      expectMsg(AuthFailure(AUTH_FAILURE_NOT_FOUND))
    }

    "fail authenticate if invalid password" in {
      val authManager = system.actorOf(Props[PipeAuthManager])
      authManager ! RegisterUser("Kostya", "akka")
      authManager ! Authenticate("Kostya", "Scala")
      expectMsg(AuthFailure(AUTH_FAILURE_PASSWORD_INCORRECT))
    }
    "successfuly authenticate" in {
      val authManager = system.actorOf(Props[PipeAuthManager])
      authManager ! RegisterUser("Kostya", "akka")
      authManager ! Authenticate("Kostya", "akka")
      expectMsg(AuthSuccess)
    }
  }
}

// this code is somewhere in app
object AskSpec {

  case class Read(key: String)

  case class Write(key: String, value: String)

  class KVActor extends Actor with ActorLogging {
    override def receive: Receive = online(Map())

    def online(kv: Map[String, String]): Receive = {
      case Read(key) =>
        log.info(s"Try Read key : $key")
        sender() ! kv.get(key)
      case Write(key, value) =>
        log.info(s"Write key : $key and value: $value ")
        context.become(online(kv + (key -> value)))
    }
  }

}

import AskSpec._

case class RegisterUser(name: String, password: String)

case class Authenticate(name: String, password: String)

case class AuthFailure(message: String)

case object AuthSuccess

object AuthManager {
  val AUTH_FAILURE_NOT_FOUND = "user not found in db"
  val AUTH_FAILURE_PASSWORD_INCORRECT = "password incorrect"
  val AUTH_FAILURE_SYSTEM = "system error"
}

class AuthManager extends Actor with ActorLogging {

  import AuthManager._

  // step 2 - logistics for ask pattern
  implicit val timeout: Timeout = Timeout(1 second)
  implicit val executorContex: ExecutionContextExecutor = context.dispatcher
  protected val dbActor = context.actorOf(Props[KVActor])

  override def receive: Receive = {
    case RegisterUser(username, password) => dbActor ! Write(username, password)
    case Authenticate(username, password) => handleAuthentication(username, password)
  }

  def handleAuthentication(username: String, password: String) = {
    val originalSender = sender()
    // step 3 - ask the actor
    val askFuture: Future[Any] = dbActor ? Read(username) // use ask when we expect single response
    // step 4 - handle the askFuture with e.g. onComplete
    askFuture.onComplete {
      // step 5 most important
      // NEVER CALL METHOD ON THE ACTOR INSTANCE OR ACCESS MUTABLE STATE IN ONCOMPLETE
      case Success(None) => originalSender ! AuthFailure(AUTH_FAILURE_NOT_FOUND) // nor use sender() - it is mutable
      case Success(Some(dbPassword)) =>
        if (dbPassword == password)
          originalSender ! AuthSuccess
        else originalSender ! AuthFailure(AUTH_FAILURE_PASSWORD_INCORRECT)
      case Failure(_) => originalSender ! AuthFailure(AUTH_FAILURE_SYSTEM)
    }
  }
}

// in practise prefer pipeTo approach
class PipeAuthManager extends AuthManager {

  import AuthManager._

  override def handleAuthentication(username: String, password: String): Unit = {
    //step 3 - ask the actor
    val askFuture: Future[Any] = dbActor ? Read(username) // use ask when we expect single response
    //step 4 - process the askFuture until you get the response you will send back
    val passwordFuture: Future[Option[String]] = askFuture.mapTo[Option[String]]
    val responseFuture = passwordFuture.map {
      case None => AuthFailure(AUTH_FAILURE_NOT_FOUND)
      case Some(dbPassword) =>
        if (dbPassword == password) AuthSuccess
        else AuthFailure(AUTH_FAILURE_PASSWORD_INCORRECT)
    } // Future[Any]
    // piping the resulting askFuture to the actor you want to send the result to
    /*
    When the askFuture completes , send the response to the actor ref in the arg list
     */
    responseFuture.pipeTo(sender())
  }
}



