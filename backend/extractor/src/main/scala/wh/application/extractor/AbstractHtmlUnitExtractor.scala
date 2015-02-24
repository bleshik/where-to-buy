package wh.application.extractor

import java.net.URL
import java.util.logging.{Level, Logger}

import com.gargoylesoftware.htmlunit.html.{HtmlAnchor, HtmlElement, HtmlImage, HtmlPage}
import com.gargoylesoftware.htmlunit.{Page, WebClient}
import com.typesafe.scalalogging.LazyLogging
import wh.extractor.domain.model.{Category, ExtractedEntry, ExtractedShop, Extractor}

import scala.util.{Failure, Success, Try}

abstract class AbstractHtmlUnitExtractor extends Extractor with LazyLogging {
  Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.SEVERE)
  protected val client = new WebClient()
  client.getOptions.setJavaScriptEnabled(false)
  client.getOptions.setCssEnabled(false)
  client.getOptions.setThrowExceptionOnFailingStatusCode(false)
  client.getCookieManager.setCookiesEnabled(true)

  override def extract(url: URL): Iterator[ExtractedEntry] = handle(Try(doExtract(client.getPage(url))))

  def doExtract(page: HtmlPage): Iterator[ExtractedEntry]

  protected def extractEntry(shop: String, city: String, name: String, price: Long, category: Category, image: HtmlElement): Option[ExtractedEntry] = {
    src(image).map(src => ExtractedEntry(ExtractedShop(shop, city), cleanUpName(name), price, category, src))
  }

  protected def filterImage(image: URL): Boolean = { true }

  protected def src(img: HtmlElement): Option[URL] = {
    Try(img.asInstanceOf[HtmlImage].getSrcAttribute).map { src: String =>
      if (!src.startsWith("http")) {
        img.getPage.getUrl.toURI.resolve(src).toURL
      } else {
        new URL(src)
      }
    }.map(src => if (filterImage(src)) Some(src) else None)
     .getOrElse(None)
  }

  protected def click(a: HtmlAnchor): Option[HtmlPage] = {
    a.click().asInstanceOf[Page] match {
      case x: HtmlPage =>
        Some(x)
      case _ =>
        None
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

  protected def handle(t: Try[Iterator[ExtractedEntry]]): Iterator[ExtractedEntry] = {
    t match {
      case Success(i) => i
      case Failure(thrown) =>
        logger.error("Coudln't load entries", thrown)
        Iterator.empty
    }
  }
}
