package wh.application.extractor

import java.net.URL
import java.util.logging.{Level, Logger}

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.gargoylesoftware.htmlunit.html._
import com.gargoylesoftware.htmlunit.util.Cookie
import com.gargoylesoftware.htmlunit.{Page, StringWebResponse, WebClient}
import com.typesafe.scalalogging.LazyLogging
import wh.extractor.domain.model.{Category, ExtractedEntry, ExtractedShop, Extractor}

import scala.io.Source
import scala.util.{Failure, Success, Try}

abstract class AbstractExtractor extends Extractor with LazyLogging {
  Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.SEVERE)
  protected lazy val json  = {
    val mapper = new ObjectMapper()
    mapper.registerModule(DefaultScalaModule)
    mapper
  }

  protected def client: WebClient = {
    val theNewClient = new WebClient()
    theNewClient.getOptions.setJavaScriptEnabled(false)
    theNewClient.getOptions.setActiveXNative(false)
    theNewClient.getOptions.setAppletEnabled(false)
    theNewClient.getOptions.setCssEnabled(false)
    theNewClient.getOptions.setThrowExceptionOnFailingStatusCode(false)
    theNewClient.getCookieManager.setCookiesEnabled(true)
    theNewClient
  }

  protected def client(url: URL, cookies: Map[String, String]): WebClient = {
    val newClient = client
    cookies.foreach { case (key, value) =>
      newClient.getCookieManager.addCookie(new Cookie(url.getHost, key, value))
    }
    newClient
  }

  override def extract(url: URL): Iterator[ExtractedEntry] = extract(url, client)

  def extract(url: URL, client: WebClient): Iterator[ExtractedEntry] =
    htmlPage(url, 3, client).map { p => handle(Try(doExtract(p)))}.flatten.getOrElse(Iterator.empty)

  def extract(url: URL, cookies: Map[String, String]): Iterator[ExtractedEntry] =
    extract(url, client(url, cookies))

  protected def page(url: URL, attempts: Int = 3, client: WebClient = client): Option[Page] = {
    if (attempts <= 0) {
      None
    } else {
      handle(Try(client.getPage(url).asInstanceOf[Page])).filter(okay).orElse(page(url, attempts - 1))
    }
  }

  protected def htmlPage(url: URL, attempts: Int = 3, client: WebClient = client): Option[HtmlPage] = page(url, attempts, client).map(_.asInstanceOf[HtmlPage])

  protected def html(html: String): HtmlPage = {
    val response = new StringWebResponse(html, new URL("http://dummy"))
    HTMLParser.parseHtml(response, client.getCurrentWindow)
  }

  protected def json[T: Manifest](url: URL, attempts: Int = 3): Option[T] = {
    handle(Try(
      json.readValue(Source.fromURL(url).reader(), manifest.runtimeClass.asInstanceOf[Class[T]])
    )).orElse(
      if (attempts > 0) json[T](url, attempts - 1) else None
    )
  }

  def doExtract(page: HtmlPage): Iterator[ExtractedEntry]

  protected def extractEntry(shop: String, city: String, name: String, price: Long, category: Category, image: HtmlElement): Option[ExtractedEntry] = {
    src(image).map(src => ExtractedEntry(ExtractedShop(shop, city), cleanUpName(name), price, category, src))
  }

  protected def filterImage(image: URL): Boolean = { true }

  protected def src(element: HtmlElement): Option[URL] = url(element, "src")

  protected def href(element: HtmlElement): Option[URL] = url(element, "href")

  private def url(img: HtmlElement, attribute: String): Option[URL] = {
    Try(img.getAttribute(attribute)).map { src: String =>
      srcToUrl(img.getPage, src)
    }.map(src => if (filterImage(src)) Some(src) else None)
     .getOrElse(None)
  }

  private def srcToUrl(page: Page, src: String): URL = {
    if (!src.contains("://")) {
      page.getUrl.toURI.resolve(src).toURL
    } else {
      new URL(src)
    }
  }

  protected def click(a: HtmlElement): Option[HtmlPage] =
    htmlPage(srcToUrl(a.getPage, a.asInstanceOf[HtmlAnchor].getHrefAttribute))

  private def okay(page: Page): Boolean = page.getWebResponse.getStatusCode >= 200 && page.getWebResponse.getStatusCode < 300

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

  protected def handle[T](t: Try[T]): Option[T] = {
    t match {
      case Success(i) => Some(i)
      case Failure(thrown) =>
        logger.error("The try tailed", thrown)
        None
    }
  }
}
