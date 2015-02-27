package wh.application.extractor.auchan

import java.net.URL

import com.gargoylesoftware.htmlunit.html._
import com.gargoylesoftware.htmlunit.util.Cookie
import wh.application.extractor.AbstractHtmlUnitExtractor
import wh.extractor.domain.model.{Category, ExtractedEntry}

import scala.collection.JavaConverters._
import scala.collection.mutable

class AuchanExtractor(val cities: Iterator[Int] = 1.to(256).iterator) extends AbstractHtmlUnitExtractor {

  override def extract(url: URL): Iterator[ExtractedEntry] = cities.flatMap { region =>
    client.getCookieManager.clearCookies()
    client.getCookieManager.addCookie(new Cookie(url.getHost, "user_shop_id", region.toString))
    super.extract(url)
  }

  override def doExtract(page: HtmlPage): Iterator[ExtractedEntry] = {
    val li = page.getBody
      .getOneHtmlElementByAttribute("ul", "class", "sub-nav")
      .asInstanceOf[HtmlElement]
      .getChildNodes
      .asScala
      .takeRight(2)
      .head
    val rootCategory = Category(cleanUpName(li.getFirstChild.getTextContent), null)
    val drop = li.getLastChild.asInstanceOf[HtmlDivision]
    val categories = drop.getOneHtmlElementByAttribute("ul", "class", "drop-ul")
      .asInstanceOf[HtmlElement]
      .getElementsByTagName("a")
      .asScala
      .asInstanceOf[mutable.Buffer[HtmlAnchor]]
      .map(c => cleanUpName(c.getTextContent))
    drop.getElementsByAttribute("div", "class", "drop2").asScala.asInstanceOf[mutable.Buffer[HtmlDivision]].zipWithIndex.iterator.flatMap { case (subDrop, i) =>
      val parentCategory = Category(categories(i), rootCategory)
      subDrop.getHtmlElementsByTagName("a").asScala.asInstanceOf[mutable.Buffer[HtmlAnchor]].flatMap { category =>
        click(category).map(page => extractCategory(page, Category(cleanUpName(category.getTextContent), parentCategory))).getOrElse(Iterator.empty)
      }
    }
  }

  private def extractCategory(page: HtmlPage, category: Category): Iterator[ExtractedEntry] = {
    val city = page.getBody
      .getOneHtmlElementByAttribute("div", "class", "city-box cf")
      .asInstanceOf[HtmlDivision]
      .getElementsByTagName("li")
      .get(0)
      .getTextContent
      .replace("г.", "")
      .trim
    page.getBody
        .getOneHtmlElementByAttribute("ul", "class", "items-list")
        .asInstanceOf[HtmlElement]
        .getElementsByTagName("li")
        .asScala
        .asInstanceOf[mutable.Buffer[HtmlListItem]]
        .iterator
        .flatMap { item =>
      val name = item.getOneHtmlElementByAttribute("div", "class", "head").asInstanceOf[HtmlDivision].getTextContent
      val price = (BigDecimal(cleanUpName(
        item.getOneHtmlElementByAttribute("div", "class", "price")
          .asInstanceOf[HtmlDivision]
          .getTextContent
          .replace("р.", "")
          .replace(",", ".")
      )) * 100).longValue()
      extractEntry("Ашан", city, name, price, category, item.getElementsByTagName("img").get(0))
    } ++
    page.getBody.getElementsByAttribute("a", "class", "next").asScala.headOption.map { nextLink: HtmlAnchor =>
      click(nextLink).map { next =>
        extractCategory(next, category)
      }.getOrElse(Iterator.empty)
    }.getOrElse(Iterator.empty)
  }
}
