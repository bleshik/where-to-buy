package wh.rest

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import com.google.inject.Guice
import spray.can.Http
import wh.application.ApiModule

import scala.concurrent.duration._

object Boot extends App {
  val injector = Guice.createInjector(new ApiModule)
  implicit val system = injector.getInstance(classOf[ActorSystem])
  val service = system.actorOf(Props[MyServiceActor])

  implicit val timeout = Timeout(5.seconds)

  // start a new HTTP server on port 8080 with our service actor as the handler
  IO(Http) ? Http.Bind(service, interface = "0.0.0.0", port = 8080)
}
