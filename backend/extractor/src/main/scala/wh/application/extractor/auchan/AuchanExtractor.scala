package wh.application.extractor.auchan

import java.net.URL

import org.jsoup.nodes.Element
import wh.application.extractor.{JsoupPage, AbstractJsoupExtractor}
import wh.extractor.domain.model.{Category, ExtractedEntry}

import scala.collection.JavaConverters._
import scala.collection.mutable
import wh.application.extractor.JsoupPage._

class AuchanExtractor extends AbstractJsoupExtractor {
  override def parts(url: URL): List[() => Iterator[ExtractedEntry]] = document(url).map { page =>
      page.document.select("ul.city-list")
        .asScala
        .asInstanceOf[mutable.Buffer[Element]]
        .headOption
        .map { cities =>
        cities.getElementsByTag("a")
          .asScala
          .asInstanceOf[mutable.Buffer[Element]]
          .map(_.attr("data-shop-id"))
          .toList
      }.map { cities =>
        cities.map { region =>
         { () => extract(url, Map(("user_shop_id", region))) }
        }
      }.getOrElse(List.empty)
    }.getOrElse(List.empty)

  override def doExtract(page: JsoupPage): Iterator[ExtractedEntry] = {
    val li = page.document.select("ul.sub-nav")
      .first()
      .children()
      .asScala
      .filter { li => !li.attr("class").trim.equals("auction") }
      .last
    val rootCategory = Category(cleanUpName(li.child(0).text()), null)
    val drop = li.children().last()
    val categories = drop.select("ul.drop-ul a")
      .asScala
      .map(c => cleanUpName(c.text))
    drop.select("div.drop2").asScala.zipWithIndex.iterator.flatMap { case (subDrop, i) =>
      val parentCategory = Category(categories(i), rootCategory)
      subDrop.select("a").asScala.iterator.flatMap { category =>
        page.click(category).map(page => extractCategory(page, Category(cleanUpName(category.text), parentCategory))).getOrElse(Iterator.empty)
      }
    }
  }

  private def extractCategory(page: JsoupPage, category: Category): Iterator[ExtractedEntry] = {
    page.document.select("div.city-box.cf li")
      .asScala
      .headOption
      .map { city =>
        city.text()
          .replace("г.", "")
          .trim
    }.map { city =>
      page.document.select("ul.items-list li")
        .asScala
        .iterator
        .flatMap { item =>
        val name = item.select("div.head").first().text
        val price = extractPrice(item.select("div.price").text)
        extractEntry("Ашан", city, name, price, category, item.select("img"))
      } ++
        page.document.select("a.next").asScala.headOption.map { nextLink =>
          page.click(nextLink).map { next =>
            extractCategory(next, category)
          }.getOrElse(Iterator.empty)
        }.getOrElse(Iterator.empty)
    }.getOrElse(Iterator.empty)
  }
}
