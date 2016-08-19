package wh.application.extractor

import java.net.{SocketTimeoutException, URL}
import java.nio.charset.StandardCharsets
import actor.domain.model.Dispatcher
import com.typesafe.scalalogging.LazyLogging
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import wh.extractor.domain.model.{Category, ExtractedEntry}
import wh.util.LoggingHandling
import java.util.concurrent.LinkedBlockingQueue
import wh.util.WaitingBlockingQueueIterator
import scala.collection.JavaConverters._
import scala.util.Try

abstract class AbstractJsoupExtractor extends AbstractExtractor with LazyLogging {

  protected def extractEntry(shop: String, city: String, name: String, price: Option[Long], category: Category, image: Element): Option[ExtractedEntry] =
    JsoupPage.url(image, "src").flatMap(src => extractEntry(shop, city, name, price, category, src))

  protected def extractEntry(shop: String, city: String, name: String, price: Option[Long], category: Category, image: Elements): Option[ExtractedEntry] =
    extractEntry(shop, city, name, price, category, image.first())
}

case class JsoupPage(document: Document, cookies: Map[String, String] = Map(), city: Option[String] = None, history: List[URL] = List()) {
  def click(a: Element): Option[JsoupPage] =
    JsoupPage.url(a, "href")
             .flatMap(u => JsoupPage.document(u, cookies, city, history :+ new URL(document.baseUri)))
             .flatMap(p => if (p.history.contains(new URL(p.document.baseUri))) None else Some(p))

  def url: URL = new URL(document.location())
}

object JsoupPage extends LoggingHandling {

  def document(e: Extract): Option[JsoupPage] = document(e.url)

  def document(e: ExtractRegion): Option[JsoupPage] = document(e.extract.url, e.attributes)

  def document(e: ExtractCategory): Option[JsoupPage] = document(e.extractRegion)

  def document(html: String): JsoupPage = JsoupPage(Jsoup.parse(html))

  def document(url: URL, cookies: Map[String, String] = Map(), city: Option[String] = None, history: List[URL] = List()): Option[JsoupPage] = {
    logger.debug(url.toString)
    handle(Try {
      if (!url.getProtocol.startsWith("http")) {
        JsoupPage(Jsoup.parse(url.openStream(), StandardCharsets.UTF_8.toString, url.toString), cookies, city, history)
      } else {
        JsoupPage(Jsoup.connect(url.toString).cookies(cookies.asJava).get(), cookies, city, history)
      }
    }, Set(classOf[SocketTimeoutException]))
  }

  def url(e: Element, attribute: String = "href"): Option[URL] = handle(Try {
    val absUrl = e.absUrl(attribute)
    val urlString = if (absUrl == null || absUrl.isEmpty) e.attr("src") else absUrl
    new URL((if (urlString.startsWith("//")) "http:" else "") + urlString)
  })
}
