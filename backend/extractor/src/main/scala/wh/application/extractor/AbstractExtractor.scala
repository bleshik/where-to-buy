package wh.application.extractor

import java.net.URL

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import wh.extractor.domain.model.{Category, ExtractedEntry, ExtractedShop, Extractor}
import wh.util.LoggingHandling

import scala.io.Source
import scala.util.Try

abstract class AbstractExtractor extends Extractor with LoggingHandling {
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

  protected def extractPrice(str: String, multiplier: Int = 100): Long = {
    (BigDecimal(cleanUpName(str.replace("р.", "").replace("руб.", "").replace(",", ".").replaceAll("[^0-9\\.]+", ""))) * multiplier).longValue()
  }

  protected def extractEntry(shop: String, city: String, name: String, price: Long, category: Category, image: URL): Option[ExtractedEntry] = {
    if (filterImage(image)) {
      Some(ExtractedEntry(ExtractedShop(shop, city), cleanUpName(name), price, category, image))
    } else {
      None
    }
  }

  protected def filterImage(image: URL): Boolean = { true }

  protected def srcToUrl(from: URL, src: String): URL = {
    if (!src.contains("://")) {
      from.toURI.resolve(src).toURL
    } else {
      new URL(src)
    }
  }
}
