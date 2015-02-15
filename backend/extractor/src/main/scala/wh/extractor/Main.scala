package wh.extractor

import java.net.URL

import akka.actor.ActorSystem
import com.typesafe.scalalogging.LazyLogging
import wh.extractor.cont.ContExtractor
import wh.extractor.komus.KomusExtractor
import wh.extractor.utkonos.UtkonosExtractor
import cronish.dsl._

object Main extends LazyLogging {
  def main(args: Array[String]): Unit = {
    if (args.length < 1) {
      throw new IllegalArgumentException("You should specify the output: 'console' or 'akka'")
    }

    logger.info("Started extractor with args: " + args.mkString(" "))

    (if (args.length > 1) args.tail.mkString(" ") else "now") match {
      case "now" => upload(args.head)
      case schedule: String => task {
        upload(args.head)
      } executes schedule
    }

    logger.info("Exiting...")
  }

  private def upload(output: String): Unit = {
    doUpload(
      List(
        ("http://www.utkonos.ru/cat", new UtkonosExtractor),
        ("http://www.komus.ru/catalog/6311/", new KomusExtractor),
        ("http://www.7cont.ru", new ContExtractor)
      ).iterator.flatMap { case (url, extractor) =>
        extractor.extract(new URL(url))
      },
      output
    )
  }

  private def doUpload(iterator: Iterator[ExtractedEntry], output: String): Unit = {
    logger.info("Started uploading to " + output)
    output match {
      case "console" => iterator.foreach(println)
      case "akka"    =>
        val akkaEndpoint = Option(System.getenv("WH_API_AKKA_ENDPOINT")).getOrElse("127.0.0.1:7000")
        logger.info("Extractor will send extracted data to " + akkaEndpoint)
        val remote = ActorSystem("ExtractorSystem")
          .actorSelection(s"akka.tcp://WhereToBuySystem@$akkaEndpoint/user/EntryExtractingActor")
        iterator.foreach(remote ! _)
    }
  }
}
