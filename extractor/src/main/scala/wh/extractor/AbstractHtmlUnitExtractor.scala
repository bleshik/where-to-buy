package wh.extractor

import java.net.URL

import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.html.HtmlPage

abstract class AbstractHtmlUnitExtractor extends Extractor{
  protected val client = new WebClient()
  client.getOptions.setJavaScriptEnabled(false)
  client.getOptions.setCssEnabled(false)
  client.getOptions.setThrowExceptionOnFailingStatusCode(false)
  client.getCookieManager.setCookiesEnabled(false)

  override def extract(url: URL): Iterator[ExtractedEntry] = doExtract(client.getPage(url))

  def doExtract(page: HtmlPage): Iterator[ExtractedEntry]

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
}
