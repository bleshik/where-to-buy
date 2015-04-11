package wh.application.extractor

import java.net.URL
import java.nio.charset.StandardCharsets

import com.typesafe.scalalogging.LazyLogging
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import wh.extractor.domain.model.{Category, ExtractedEntry}

import scala.collection.JavaConverters._
import scala.util.Try

abstract class AbstractJsoupExtractor extends AbstractExtractor with LazyLogging {
  override def extract(url: URL): Iterator[ExtractedEntry] = extract(url, Map())

  def extract(url: URL, cookies: Map[String, String]): Iterator[ExtractedEntry] =
    document(url, cookies).map(doExtract).getOrElse(Iterator.empty)

  protected def document(url: URL, cookies: Map[String, String] = Map()): Option[Document] =
    handle(Try {
      if (!url.getProtocol.startsWith("http")) {
        Jsoup.parse(url.openStream(), StandardCharsets.UTF_8.toString, url.toString)
      } else {
        Jsoup.connect(url.toString).cookies(cookies.asJava).get()
      }
    })

  protected def document(html: String): Document = Jsoup.parse(html)

  protected def extractEntry(shop: String, city: String, name: String, price: Long, category: Category, image: Element): Option[ExtractedEntry] =
    handle(Try(new URL(image.absUrl("src")))).flatMap(src => extractEntry(shop, city, name, price, category, src))

  protected def extractEntry(shop: String, city: String, name: String, price: Long, category: Category, image: Elements): Option[ExtractedEntry] =
    extractEntry(shop, city, name, price, category, image.first())

  protected def click(a: Element): Option[Document] = url(a, "href").flatMap(document(_))

  protected def url(e: Element, attribute: String): Option[URL] = handle(Try(new URL(e.absUrl(attribute))))

  def doExtract(page: Document): Iterator[ExtractedEntry]
}
