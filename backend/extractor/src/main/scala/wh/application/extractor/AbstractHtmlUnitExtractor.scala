package wh.application.extractor

import java.net.URL
import java.util.logging.{Level, Logger}

import com.gargoylesoftware.htmlunit.html._
import com.gargoylesoftware.htmlunit.util.Cookie
import com.gargoylesoftware.htmlunit.{Page, StringWebResponse, WebClient}
import com.typesafe.scalalogging.LazyLogging
import wh.extractor.domain.model.{Category, ExtractedEntry}

import scala.util.Try

abstract class AbstractHtmlUnitExtractor extends AbstractExtractor with LazyLogging {
  Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.SEVERE)

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

  def extract(url: URL, client: WebClient): Iterator[ExtractedEntry] = {
    val it = htmlPage(url, 3, client).flatMap { p => handle(Try(doExtract(p))) }.getOrElse(Iterator.empty)
    new Iterator[ExtractedEntry] {
      @volatile var i = 0

      override def hasNext: Boolean = it.hasNext

      override def next(): ExtractedEntry = {
        val n = it.next()
        i += 1
        if (i % 50 == 0) {
          logger.info(s"Extracted $i entries from $url with cookies ${client.getCookies(url)}")
        }
        n
      }
    }
  }

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

  def doExtract(page: HtmlPage): Iterator[ExtractedEntry]

  protected def extractEntry(shop: String, city: String, name: String, price: Long, category: Category, image: HtmlElement): Option[ExtractedEntry] = {
    src(image).map(src => extractEntry(shop, city, name, price, category, src))
  }

  protected def filterImage(image: URL): Boolean = { true }

  protected def src(element: HtmlElement): Option[URL] = url(element, "src")

  protected def href(element: HtmlElement): Option[URL] = url(element, "href")

  private def url(img: HtmlElement, attribute: String): Option[URL] = {
    Try(img.getAttribute(attribute)).map { src: String =>
      srcToUrl(img.getPage.getUrl, src)
    }.map(src => if (filterImage(src)) Some(src) else None)
     .getOrElse(None)
  }

  protected def click(a: HtmlElement): Option[HtmlPage] =
    htmlPage(srcToUrl(a.getPage.getUrl, a.asInstanceOf[HtmlAnchor].getHrefAttribute))

  private def okay(page: Page): Boolean = page.getWebResponse.getStatusCode >= 200 && page.getWebResponse.getStatusCode < 300
}
