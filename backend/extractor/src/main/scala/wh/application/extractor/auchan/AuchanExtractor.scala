package wh.application.extractor.auchan

import java.net.URL

import org.jsoup.nodes.{Document, Element}
import wh.application.extractor.AbstractJsoupExtractor
import wh.extractor.domain.model.{Category, ExtractedEntry}

import scala.collection.JavaConverters._
import scala.collection.mutable

class AuchanExtractor extends AbstractJsoupExtractor {

  override def extract(url: URL): Iterator[ExtractedEntry] = document(url).map { page =>
    page.select("ul.city-list")
      .asScala
      .asInstanceOf[mutable.Buffer[Element]]
      .headOption
      .map { cities =>
        cities.getElementsByTag("a")
          .asScala
          .asInstanceOf[mutable.Buffer[Element]]
          .iterator
          .map(_.attr("data-shop-id"))
    }.map { cities =>
      cities.flatMap { region =>
        super.extract(url, Map(("user_shop_id", region)))
      }
    }.getOrElse(Iterator.empty)
  }.getOrElse(Iterator.empty)

  override def doExtract(page: Document): Iterator[ExtractedEntry] = {
    val li = page.select("ul.sub-nav")
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
        click(category).map(page => extractCategory(page, Category(cleanUpName(category.text), parentCategory))).getOrElse(Iterator.empty)
      }
    }
  }

  private def extractCategory(page: Document, category: Category): Iterator[ExtractedEntry] = {
    page.select("div.city-box.cf li")
      .asScala
      .headOption
      .map { city =>
        city.text()
          .replace("г.", "")
          .trim
    }.map { city =>
      page.select("ul.items-list li")
        .asScala
        .iterator
        .flatMap { item =>
        val name = item.select("div.head").first().text
        val price = extractPrice(item.select("div.price").text)
        extractEntry("Ашан", city, name, price, category, item.select("img"))
      } ++
        page.select("a.next").asScala.headOption.map { nextLink =>
          click(nextLink).map { next =>
            extractCategory(next, category)
          }.getOrElse(Iterator.empty)
        }.getOrElse(Iterator.empty)
    }.getOrElse(Iterator.empty)
  }
}
