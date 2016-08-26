package wh.application.extractor

import actor.domain.model.Dispatcher
import actor.domain.model.EventTransport
import actor.port.adapter.local.FilteringEventTransport
import actor.port.adapter.local.LocalEventTransport
import com.typesafe.scalalogging.LazyLogging
import java.net.URL
import java.util.Collections
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import scala.compat.java8.FunctionConverters._
import scala.concurrent.forkjoin.ForkJoinPool
import scala.util.Try
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
import wh.extractor.domain.model.ExtractedRegion
import wh.util.ConcurrencyUtil._

object ExtractorApp extends LazyLogging {
  def main(args: Array[String]): Unit = {
    logger.info("Started extractor with args: " + args.mkString(" "))

    try {
      val dispatcher = extract(new LocalEventTransport(), (entry) => println(entry))
      Thread.sleep(1000)
      dispatcher.close()
    } catch {
      case e: Exception => logger.error("Extractor failed", e)
    }

    logger.info("Exiting...");
  }

  def extract(eventTransport: EventTransport, callback: (ExtractedEntry) => Unit): Dispatcher = {
    val dispatcher = new Dispatcher(new FilteringEventTransport(
      eventTransport,
      { (event: EventTransport.Event)  =>
          if (event.payload.isInstanceOf[ExtractRegion]) {
            ExtractedRegion(event.payload.asInstanceOf[ExtractRegion].region)
              .city
              .map { Environment.cities.contains(_) }
              .getOrElse(false)
          } else if (event.payload.isInstanceOf[ExtractCategory]) {
            ExtractedRegion(event.payload.asInstanceOf[ExtractCategory].extractRegion.region)
              .city
              .map { Environment.cities.contains(_) }
              .getOrElse(false)
          } else { true }
      }.asJava,
      { (event: EventTransport.Event)  =>
          if (event.payload.isInstanceOf[ExtractRegion]) {
            LoggerFactory.getLogger(event.actorClass).info("Started extracting " +
              event.payload.asInstanceOf[ExtractRegion].region)
          } else if (event.payload.isInstanceOf[ExtractCategory]) {
            LoggerFactory.getLogger(event.actorClass).info("Started extracting " +
              event.payload.asInstanceOf[ExtractCategory].category)
          }
          true
      }.asJava
    ))
    if (Environment.cities.nonEmpty) {
      logger.info(s"Extract only for cities ${Environment.cities}")
    }
    if (Environment.shops.nonEmpty) {
      logger.info(s"Extract only for shops ${Environment.shops}")
    }
    List(
      ("http://av.ru/food/all/", classOf[AvExtractor]),
      ("http://av.ru/nonfood/", classOf[AvExtractor]),
      ("http://klg.metro-cc.ru", classOf[MetroExtractor]),
      ("http://www.auchan.ru", classOf[AuchanExtractor]),
      ("http://www.utkonos.ru/cat", classOf[UtkonosExtractor]),
      ("http://www.komus.ru", classOf[KomusExtractor]),
      ("http://www.7cont.ru", classOf[ContExtractor]),
      ("https://dixy.ru/promo/", classOf[DixyExtractor]),
      ("http://globusgurme.ru/catalog", classOf[GlobusGurmeExtractor])
    ).filter { e =>
      Environment.shops.isEmpty || Environment.shops.get.exists { shop => e._1.toLowerCase.contains(shop.toLowerCase) }
    }.foreach { e =>
      val extractMsg = Extract(new URL(e._1), callback)
      logger.info("Sending " + extractMsg + " to " + e._2)
      dispatcher.send(e._2, extractMsg)
    }
    dispatcher
  }
}
