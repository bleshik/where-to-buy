package wh.extractor

import java.net.URL

import com.gargoylesoftware.htmlunit.{Page, WebClient}
import com.gargoylesoftware.htmlunit.html.{HtmlAnchor, HtmlPage}
import com.typesafe.scalalogging.LazyLogging

import scala.util.{Failure, Success, Try}

abstract class AbstractHtmlUnitExtractor extends Extractor with LazyLogging {
  protected val client = new WebClient()
  client.getOptions.setJavaScriptEnabled(false)
  client.getOptions.setCssEnabled(false)
  client.getOptions.setThrowExceptionOnFailingStatusCode(false)
  client.getCookieManager.setCookiesEnabled(false)

  override def extract(url: URL): Iterator[ExtractedEntry] = handle(Try(doExtract(client.getPage(url))))

  def doExtract(page: HtmlPage): Iterator[ExtractedEntry]

  protected def extractEntry(name: String, price: Long, category: Category): ExtractedEntry = {
    ExtractedEntry(this.getClass.getSimpleName.replace("Extractor", ""), name, price, category)
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
    val noDotsStr = str.replace("…»", "").trim
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
      case Failure(thrown) => {
        logger.error("Coudln't load entries", thrown)
        Iterator.empty
      }
    }
  }
}
