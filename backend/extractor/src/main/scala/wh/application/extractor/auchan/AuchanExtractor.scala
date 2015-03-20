package wh.application.extractor.auchan

import java.net.URL

import com.gargoylesoftware.htmlunit.html._
import com.gargoylesoftware.htmlunit.util.Cookie
import wh.application.extractor.AbstractExtractor
import wh.extractor.domain.model.{Category, ExtractedEntry}

import scala.collection.JavaConverters._
import scala.collection.mutable

class AuchanExtractor extends AbstractExtractor {

  override def extract(url: URL): Iterator[ExtractedEntry] = htmlPage(url).map { page =>
    page.getBody
      .getElementsByAttribute("ul", "class", "city-list")
      .asScala
      .asInstanceOf[mutable.Buffer[HtmlElement]]
      .headOption
      .map { cities =>
        cities.getElementsByTagName("a")
          .asScala
          .asInstanceOf[mutable.Buffer[HtmlElement]]
          .iterator
          .map(_.getAttribute("data-shop-id"))
    }.map { cities =>
      cities.flatMap { region =>
        client.getCookieManager.clearCookies()
        client.getCookieManager.addCookie(new Cookie(url.getHost, "user_shop_id", region))
        super.extract(url)
      }
    }.getOrElse(Iterator.empty)
  }.getOrElse(Iterator.empty)

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
    drop.getElementsByAttribute("div", "class", "drop2").asScala.asInstanceOf[mutable.Buffer[HtmlDivision]].zipWithIndex.flatMap { case (subDrop, i) =>
      val parentCategory = Category(categories(i), rootCategory)
      subDrop.getHtmlElementsByTagName("a").asScala.asInstanceOf[mutable.Buffer[HtmlAnchor]].flatMap { category =>
        click(category).map(page => extractCategory(page, Category(cleanUpName(category.getTextContent), parentCategory))).getOrElse(Iterator.empty)
      }
    }.iterator
  }

  private def extractCategory(page: HtmlPage, category: Category): Iterator[ExtractedEntry] = {
    page.getBody
      .getElementsByAttribute("div", "class", "city-box cf")
      .asScala
      .asInstanceOf[mutable.Buffer[HtmlDivision]]
      .headOption
      .map { city =>
        city.getElementsByTagName("li")
          .get(0)
          .getTextContent
          .replace("г.", "")
          .trim
    }.map { city =>
      page.getBody
        .getOneHtmlElementByAttribute("ul", "class", "items-list")
        .asInstanceOf[HtmlElement]
        .getElementsByTagName("li")
        .asScala
        .asInstanceOf[mutable.Buffer[HtmlListItem]]
        .iterator
        .flatMap { item =>
        val name = item.getOneHtmlElementByAttribute("div", "class", "head").asInstanceOf[HtmlDivision].getTextContent
        val price = extractPrice(item.getOneHtmlElementByAttribute("div", "class", "price").asInstanceOf[HtmlDivision].getTextContent)
        extractEntry("Ашан", city, name, price, category, item.getElementsByTagName("img").get(0))
      } ++
        page.getBody.getElementsByAttribute("a", "class", "next").asScala.headOption.map { nextLink: HtmlAnchor =>
          click(nextLink).map { next =>
            extractCategory(next, category)
          }.getOrElse(Iterator.empty)
        }.getOrElse(Iterator.empty)
    }.getOrElse(Iterator.empty)
  }
}
