package wh.application.extractor.auchan

import java.net.URL
import java.util.concurrent.LinkedBlockingQueue
import org.jsoup.nodes.Element
import wh.application.extractor.{JsoupPage, AbstractJsoupExtractor}
import wh.extractor.domain.model.{Category, ExtractedEntry}
import wh.application.extractor.Extract
import wh.application.extractor.ExtractCategory
import wh.application.extractor.ExtractCity
import wh.util.WaitingBlockingQueueIterator
import scala.collection.JavaConverters._
import scala.collection.mutable
import wh.application.extractor.JsoupPage._

class AuchanExtractor extends AbstractJsoupExtractor {

  override protected def when(e: Extract): Unit =
    document(e).map { page =>
      page.document.select("ul.city-list")
        .asScala
        .asInstanceOf[mutable.Buffer[Element]]
        .headOption
        .foreach { cities =>
          cities.getElementsByTag("a")
            .asScala
            .asInstanceOf[mutable.Buffer[Element]]
            .foreach { shop =>
              sendToMyself(
                ExtractCity(shop.text().trim(), e, Map(("user_shop_id", shop.attr("data-shop-id"))))
              )
            }
        }
      }

  protected def when(e: ExtractCity): Unit = {
    document(e).foreach { page =>
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
      drop.select("div.drop2").asScala.zipWithIndex.foreach { case (subDrop, i) =>
        val parentCategory = Category(categories(i), rootCategory)
        subDrop.select("a").asScala.foreach { category =>
          JsoupPage.url(category, "href").map(url =>
            sendToMyself(
              ExtractCategory(
                Category(cleanUpName(category.text), parentCategory),
                e.withUrl(url)
              )
            )
          )
        }
      }
    }
  }

  protected def when(e: ExtractCategory): Unit = {
    document(e).foreach { page =>
      page.document.select("ul.items-list li")
        .asScala
        .foreach { item =>
        val name = item.select("div.head").first().text
        val price = extractPrice(item.select("div.price").text)
        extractEntry("Ашан", e.extractCity.city, name, price, e.category, item.select("img")).foreach { entry =>
          e.extractCity.extract.callback(entry)
        }
      }
      page.document.select("a.next").asScala.headOption.map { nextLink =>
        JsoupPage.url(nextLink, "href").map { url =>
          sendToMyself(e.withUrl(url))
        }
      }
    }
  }

}
