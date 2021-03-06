package wh.application.extractor

import actor.domain.model.Actor
import actor.domain.model.Dispatcher
import actor.port.adapter.local.LocalEventTransport
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import java.net.URL
import java.util.concurrent.LinkedBlockingQueue
import scala.io.Source
import scala.util.Try
import wh.application.extractor.infrastructure.Environment
import wh.extractor.domain.model.{Category, ExtractedEntry, ExtractedShop, Extractor}
import wh.util.LoggingHandling
import wh.util.WaitingBlockingQueueIterator

abstract class AbstractExtractor extends Actor with Extractor with LoggingHandling {
  protected lazy val json  = {
    val mapper = new ObjectMapper()
    mapper.registerModule(DefaultScalaModule)
    mapper
  }

  protected def json[T: Manifest](url: URL, attempts: Int = 3): Option[T] = {
    handle(Try(
      json.readValue(Source.fromURL(url).reader(), manifest.runtimeClass.asInstanceOf[Class[T]])
    )).orElse(
        if (attempts > 0) json[T](url, attempts - 1) else None
      )
  }

  protected def cleanUpName(str: String): String = {
    if (str == null || str.isEmpty) {
      ""
    } else {
      val noDotsStr = str.replace("…»", "").replaceAll("(?m)\\s+", " ").trim
      if (!noDotsStr.head.isSpaceChar && !noDotsStr.head.isWhitespace) {
        if (!noDotsStr.last.isSpaceChar && !noDotsStr.head.isWhitespace) {
          noDotsStr
        } else {
          cleanUpName(noDotsStr.take(noDotsStr.length - 1))
        }
      } else {
        cleanUpName(noDotsStr.tail)
      }
    }
  }

  protected def extractPrice(str: String, multiplier: Int = 100): Option[Long] = {
    val extractedPrice = cleanUpName(str.replace("р.", "").replace("руб.", "").replace(",", ".").replaceAll("[^0-9\\.]+", ""))
    try {
      Some((BigDecimal(extractedPrice) * multiplier).longValue())
    } catch {
      case e: NumberFormatException =>
        logger.warn(s"Couldn't parse a price in the string '$str', my attempt was '$extractedPrice'", e)
        None
    }
  }

  protected def extractEntry(shop: String, city: String, name: String, price: Option[Long], category: Category, image: URL): Option[ExtractedEntry] = {
    price.flatMap { price =>
      if (filterImage(image)) {
        Some(ExtractedEntry(new ExtractedShop(shop, city), cleanUpName(name), price, category, image))
      } else {
        None
      }
    }
  }

  protected def filterImage(image: URL): Boolean = {
    image != null && !Set("noimage", "notfound", "none").exists(image.toString.toLowerCase.contains)
  }

  def extract(url: URL): Iterator[ExtractedEntry] = {
    val queue = new LinkedBlockingQueue[ExtractedEntry]()
    extract(url, (e) => e.foreach(queue.add(_)));
    new WaitingBlockingQueueIterator(queue)
  }

  override def extract(url: URL, callback: (Seq[ExtractedEntry]) => Unit): Unit = {
    new Dispatcher(new LocalEventTransport()).send(this.getClass(), Extract(url, callback))
  }
}
