package wh.application.extractor

import java.net.URL
import java.util.Collections

import akka.actor.ActorSystem
import com.typesafe.config.{ConfigFactory, ConfigParseOptions, ConfigResolveOptions, ConfigValueFactory}
import com.typesafe.scalalogging.LazyLogging
import wh.application.extractor.auchan.AuchanExtractor
import wh.application.extractor.av.AvExtractor
import wh.application.extractor.cont.ContExtractor
import wh.application.extractor.dixy.DixyExtractor
import wh.application.extractor.globusgurme.GlobusGurmeExtractor
import wh.application.extractor.infrastructure.Environment
import wh.application.extractor.komus.KomusExtractor
import wh.application.extractor.metro.MetroExtractor
import wh.application.extractor.utkonos.UtkonosExtractor
import wh.extractor.domain.model.ExtractedEntry
import wh.util.ConcurrencyUtil._

import scala.concurrent.forkjoin.ForkJoinPool
import scala.util.Try

object ExtractorApp extends LazyLogging {
  def main(args: Array[String]): Unit = {
    if (args.length < 1) {
      throw new IllegalArgumentException("You should specify the output: 'console' or 'dynamo'")
    }

    logger.info("Started extractor with args: " + args.mkString(" "))

    try {
      extract(args.head)
    } catch {
      case e: Exception => logger.error("Extractor failed", e)
    }

    logger.info("Exiting...");
  }

  private def extract(output: String): Unit = {
    if (Environment.cities.nonEmpty) {
      logger.info(s"Extract only for cities ${Environment.cities}")
    }
    if (Environment.shops.nonEmpty) {
      logger.info(s"Extract only for shops ${Environment.shops}")
    }
    List(
      ("http://av.ru/food/all/", new AvExtractor),
      ("http://av.ru/nonfood/", new AvExtractor),
      ("http://klg.metro-cc.ru", new MetroExtractor),
      ("http://www.auchan.ru", new AuchanExtractor),
      ("http://www.utkonos.ru/cat", new UtkonosExtractor),
      ("http://www.komus.ru", new KomusExtractor),
      ("http://www.7cont.ru", new ContExtractor),
      ("https://dixy.ru/promo/", new DixyExtractor),
      ("http://globusgurme.ru/catalog", new GlobusGurmeExtractor)
    ).filter { e =>
      Environment.shops.isEmpty || Environment.shops.get.exists { shop => e._1.toLowerCase.contains(shop.toLowerCase) }
    }.foreach { e =>
      e._2.extract(new URL(e._1), (entry) => doUpload(entry, output))
    }
    Thread.sleep(1000)
    Environment.dispatcher.awaitTermination()
  }

  private def doUpload(entry: ExtractedEntry, output: String): Unit = {
    output match {
      case "none" =>
      case "console" => println(entry)
    }
  }
}
