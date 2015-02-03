package wh.extractor

import java.io.ByteArrayOutputStream
import java.net.URL
import javax.imageio.ImageIO

import com.gargoylesoftware.htmlunit.html.{HtmlAnchor, HtmlElement, HtmlImage, HtmlPage}
import com.gargoylesoftware.htmlunit.{Page, WebClient}
import com.typesafe.scalalogging.LazyLogging

import scala.util.{Failure, Success, Try}

abstract class AbstractHtmlUnitExtractor(val downloadImages: Boolean) extends Extractor with LazyLogging {
  protected val client = new WebClient()
  client.getOptions.setJavaScriptEnabled(false)
  client.getOptions.setCssEnabled(false)
  client.getOptions.setThrowExceptionOnFailingStatusCode(false)
  client.getCookieManager.setCookiesEnabled(false)

  override def extract(url: URL): Iterator[ExtractedEntry] = handle(Try(doExtract(client.getPage(url))))

  def doExtract(page: HtmlPage): Iterator[ExtractedEntry]

  protected def extractEntry(name: String, price: Long, category: Category, image: Option[Array[Byte]]): ExtractedEntry = {
    ExtractedEntry(this.getClass.getSimpleName.replace("Extractor", ""), name, price, category, image)
  }

  protected def download(img: HtmlElement): Option[Array[Byte]] = {
    if (downloadImages) {
      Try({
        def out = new ByteArrayOutputStream()
        def image = img.asInstanceOf[HtmlImage].getImageReader
        ImageIO.write(image.read(0), image.getFormatName, out)
        Some(out.toByteArray)
      }).getOrElse(None)
    } else {
      None
    }
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
      case Failure(thrown) =>
        logger.error("Coudln't load entries", thrown)
        Iterator.empty
    }
  }
}
