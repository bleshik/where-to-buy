package wh.application.extractor

import java.net.URL

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.gargoylesoftware.htmlunit.html.HtmlElement
import com.typesafe.scalalogging.LazyLogging
import wh.extractor.domain.model.{ExtractedShop, ExtractedEntry, Category, Extractor}

import scala.io.Source
import scala.util.{Failure, Success, Try}

abstract class AbstractExtractor extends Extractor with LazyLogging {
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

  protected def handle[T](t: Try[T]): Option[T] = {
    t match {
      case Success(i) => Some(i)
      case Failure(thrown) => thrown match {
        case exception: Exception =>
          logger.error("The try tailed", thrown)
          None
        case other: Throwable =>
          throw other
      }
    }
  }

  protected def cleanUpName(str: String): String = {
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

  protected def extractPrice(str: String, multiplier: Int = 100): Long = {
    (BigDecimal(cleanUpName(str.replace("р.", "").replace("руб.", "").replace(",", ".").replaceAll("[^0-9\\.]+", ""))) * multiplier).longValue()
  }

  protected def extractEntry(shop: String, city: String, name: String, price: Long, category: Category, image: URL): ExtractedEntry = {
    ExtractedEntry(ExtractedShop(shop, city), cleanUpName(name), price, category, image)
  }

  protected def srcToUrl(from: URL, src: String): URL = {
    if (!src.contains("://")) {
      from.toURI.resolve(src).toURL
    } else {
      new URL(src)
    }
  }
}
