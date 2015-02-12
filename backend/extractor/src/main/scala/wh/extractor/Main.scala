package wh.extractor

import java.net.URL

import akka.actor.ActorSystem
import wh.extractor.cont.ContExtractor
import wh.extractor.komus.KomusExtractor
import wh.extractor.utkonos.UtkonosExtractor
import cronish.dsl._

object Main {
  def main(args: Array[String]): Unit = {
    if (args.length < 1) {
      throw new IllegalArgumentException("You should specify the output: 'console' or 'akka'")
    }

    (if (args.length > 1) args.tail.mkString(" ") else "now") match {
      case "now" => upload(args.head)
      case schedule: String => task {
        upload(args.head)
      } executes schedule
    }


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
    output match {
      case "console" => iterator.foreach(println)
      case "akka"    =>
        val remote = ActorSystem("ExtractorSystem").actorSelection("akka.tcp://WhereToBuySystem@127.0.0.1:7000/user/EntryExtractingActor")
        iterator.foreach(remote ! _)
    }
  }
}
