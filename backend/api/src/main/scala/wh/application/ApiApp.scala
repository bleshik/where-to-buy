package wh.application

import _root_.akka.actor.{ActorRef, ActorSystem}
import _root_.akka.io.IO
import _root_.akka.pattern.ask
import _root_.akka.util.Timeout
import com.google.inject.name.Names
import com.google.inject.{Guice, Key}
import com.typesafe.scalalogging.LazyLogging
import spray.can.Http
import wh.infrastructure.Environment
import wh.util.LoggingUtil

import scala.concurrent.duration._

object ApiApp extends App with LazyLogging {
  logger.debug(s"Starting Where To Buy app in ${Environment.current} environment")
  LoggingUtil.logMemory("api")
  val injector = Guice.createInjector(Environment.stage, new ApiModule)

  implicit val system = injector.getInstance(classOf[ActorSystem])
  val service = injector.getInstance(Key.get(classOf[ActorRef], Names.named("RestActor")))

  implicit val timeout = Timeout(5.seconds)

  // start a new HTTP server on port 8080 with our service actor as the handler
  IO(Http) ? Http.Bind(service, interface = "0.0.0.0", port = 8080)
}
