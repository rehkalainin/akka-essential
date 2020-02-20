package actors

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import com.typesafe.config.{Config, ConfigFactory}

object IntroAkkaConfig extends App {

  class SympleLoggingActor extends Actor with ActorLogging{
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }

  /**
   * 1 - inline configuration inside current file
   */

val configString =
  """
    |akka{
    |   loglevel = "INFO"
    |}
    |""".stripMargin

  val config: Config = ConfigFactory.parseString(configString)
  val system = ActorSystem("ConfigurationDemo", ConfigFactory.load(config))
  val actor = system.actorOf(Props[SympleLoggingActor])

  actor ! "A message to remember"

  /**
   * 2 - default config file for all actorSystems
   */


  val defaultConfigFileSystem = ActorSystem("DefaultConfigFileSystem") // akka default loading config from resources "application.conf"
  val defaultActorConfigFile = defaultConfigFileSystem.actorOf(Props[SympleLoggingActor])
  defaultActorConfigFile ! "Remember me "

  /**
   * 3 - if I need be configurable each actorSystem differently
   *    separate config from the same default application.conf file
   */
  val specialConfig = ConfigFactory.load().getConfig("mySpecialConfig")
  val specialActorConfigSystem = ActorSystem("SpecialConfigSystem", specialConfig)
  val specialActorConfig = specialActorConfigSystem.actorOf(Props[SympleLoggingActor])

  specialActorConfig ! "Remember me! I am special"
  /**
   * 4 - special separate configuration from another file
   */
  val separeteConfig = ConfigFactory.load("secretFolder/secretConfiguration.conf")
  println(s"separate config log level:  ${separeteConfig.getString("akka.loglevel")}")

  /**
   * 5 - different format file
   *    JSON format
   */
  val jsonConfig = ConfigFactory.load("json/jsonConfig.json")
  println(s"json config: ${jsonConfig.getString("aJsonProperties")}")
  println(s"json config: ${jsonConfig.getString("akka.loglevel")}")

}
