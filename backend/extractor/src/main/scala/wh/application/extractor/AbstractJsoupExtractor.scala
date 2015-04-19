package wh.application.extractor

import java.net.URL
import java.nio.charset.StandardCharsets

import com.typesafe.scalalogging.LazyLogging
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import wh.extractor.domain.model.{Category, ExtractedEntry}
import wh.util.LoggingHandling

import scala.collection.JavaConverters._
import scala.util.Try

abstract class AbstractJsoupExtractor extends AbstractExtractor with LazyLogging {
  override def extract(url: URL): Iterator[ExtractedEntry] = extract(url, Map())

  def extract(url: URL, cookies: Map[String, String], city: Option[String] = None): Iterator[ExtractedEntry] = {
    val it = JsoupPage.document(url, cookies, city).map(doExtract).getOrElse(Iterator.empty)
    new Iterator[ExtractedEntry] {
      @volatile var i = 0

      override def hasNext: Boolean = it.hasNext

      override def next(): ExtractedEntry = {
        val n = it.next()
        if (i == 0) {
          logger.debug(s"Starting extracting $url (${n.shop.city}) with cookies $cookies")
        }
        i += 1
        if (i % 50 == 0) {
          logger.info(s"Extracted $i entries (last category is ${n.category.name}}) from $url (${n.shop.city}) with cookies $cookies")
        }
        if (!it.hasNext) {
          logger.debug(s"Done extracting $url (${n.shop.city}) with cookies $cookies")
        }
        n
      }
    }
  }

  protected def extractEntry(shop: String, city: String, name: String, price: Long, category: Category, image: Element): Option[ExtractedEntry] =
    handle(Try(new URL(image.absUrl("src")))).flatMap(src => extractEntry(shop, city, name, price, category, src))

  protected def extractEntry(shop: String, city: String, name: String, price: Long, category: Category, image: Elements): Option[ExtractedEntry] =
    extractEntry(shop, city, name, price, category, image.first())

  def doExtract(page: JsoupPage): Iterator[ExtractedEntry]
}

case class JsoupPage(document: Document, cookies: Map[String, String] = Map(), city: Option[String] = None, history: List[URL] = List()) {
  def click(a: Element): Option[JsoupPage] =
    JsoupPage.url(a, "href")
             .flatMap(u => JsoupPage.document(u, cookies, city, history :+ new URL(document.baseUri)))
             .flatMap(p => if (p.history.contains(new URL(p.document.baseUri))) None else Some(p))
}

object JsoupPage extends LoggingHandling {
  def document(html: String): JsoupPage = JsoupPage(Jsoup.parse(html))

  def document(url: URL, cookies: Map[String, String] = Map(), city: Option[String] = None, history: List[URL] = List()): Option[JsoupPage] = {
    logger.debug(url.toString)
    handle(Try {
      if (!url.getProtocol.startsWith("http")) {
        JsoupPage(Jsoup.parse(url.openStream(), StandardCharsets.UTF_8.toString, url.toString), cookies, city, history)
      } else {
        JsoupPage(Jsoup.connect(url.toString).cookies(cookies.asJava).get(), cookies, city, history)
      }
    })
  }

  def url(e: Element, attribute: String): Option[URL] = handle(Try(new URL(e.absUrl(attribute))))
}
