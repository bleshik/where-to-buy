package wh.rest

import akka.actor.{ActorRef, ActorSystem}
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import com.google.inject.name.Names
import com.google.inject.{Guice, Key}
import spray.can.Http
import wh.application.ApiModule

import scala.concurrent.duration._

object Boot extends App {
  val injector = Guice.createInjector(new ApiModule)
  implicit val system = injector.getInstance(classOf[ActorSystem])
  val service = injector.getInstance(Key.get(classOf[ActorRef], Names.named("MyServiceActor")))

  implicit val timeout = Timeout(5.seconds)

  // start a new HTTP server on port 8080 with our service actor as the handler
  IO(Http) ? Http.Bind(service, interface = "0.0.0.0", port = 8080)
}
