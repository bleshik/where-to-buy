package wh.application

import _root_.akka.actor.{ActorRef, ActorSystem}
import _root_.akka.io.IO
import _root_.akka.pattern.ask
import _root_.akka.util.Timeout
import com.google.inject.Injector
import com.google.inject.name.Names
import com.google.inject.{Guice, Key}
import com.typesafe.scalalogging.LazyLogging
import scala.concurrent.duration._
import spray.can.Http
import wh.infrastructure.Environment
import wh.application.extractor.ExtractorApp
import wh.application.extractor.ExtractedEntryHandler

object ApiApp extends LazyLogging {

  lazy val injector: Injector = Guice.createInjector(Environment.stage, new ApiModule)

  def main(args: Array[String]): Unit = {
    if (args.headOption.equals(Option("extractor"))) {
      ExtractorApp.extractLocally((entries) =>
          entries.foreach((entry) => injector.getInstance(classOf[ExtractedEntryHandler]).handle(entry))
      )
    } else {
      logger.info(s"Starting Where To Buy app in ${Environment.current} environment")
      implicit val system = injector.getInstance(classOf[ActorSystem])
      val service = injector.getInstance(Key.get(classOf[ActorRef], Names.named("RestActor")))

      implicit val timeout = Timeout(5.seconds)

      // start a new HTTP server on port 8080 with our service actor as the handler
      IO(Http) ? Http.Bind(service, interface = "0.0.0.0", port = 8080)
    }
  }

}
