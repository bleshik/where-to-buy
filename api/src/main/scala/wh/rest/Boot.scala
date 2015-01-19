package wh.rest

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import spray.can.Http
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import wh.application.extractor.EntryExtractingActor

object Boot extends App {
  // we need an ActorSystem to host our application in
  implicit val system = ActorSystem("WhereToBuySystem")

  val service = system.actorOf(Props[MyServiceActor], "bills")
  val remote = system.actorOf(Props[EntryExtractingActor], "entry")

  implicit val timeout = Timeout(5.seconds)

  // start a new HTTP server on port 8080 with our service actor as the handler
  IO(Http) ? Http.Bind(service, interface = "0.0.0.0", port = 8080)
}
