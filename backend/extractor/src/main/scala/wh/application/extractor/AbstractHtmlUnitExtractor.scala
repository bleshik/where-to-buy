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

  override def extract(url: URL): Iterator[ExtractedEntry] = extract(url, 3)

  private def extract(url: URL, attempts: Int): Iterator[ExtractedEntry] = {
    if (attempts <= 0) {
      Iterator.empty
    } else {
      handle(Try(client.getPage(url).asInstanceOf[HtmlPage])).map { x: HtmlPage =>
        if (okay(x)) doExtract(x) else extract(url, attempts - 1)
      }.getOrElse(extract(url, attempts - 1))
    }
  }

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

  protected def click(a: HtmlAnchor, attempts: Int = 3): Option[HtmlPage] = {
    if (attempts <= 0) {
      None
    } else {
      a.click().asInstanceOf[Page] match {
        case x: HtmlPage =>
          if (okay(x)) {
            Some(x)
          } else {
            click(a, attempts - 1)
          }
        case _ =>
          None
      }
    }
  }

  private def okay(page: HtmlPage): Boolean = page.getWebResponse.getStatusCode >= 200 && page.getWebResponse.getStatusCode < 300

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

  protected def handle[T](t: Try[T]): Option[T] = {
    t match {
      case Success(i) => Some(i)
      case Failure(thrown) =>
        logger.error("The try tailed", thrown)
        None
    }
  }
}
