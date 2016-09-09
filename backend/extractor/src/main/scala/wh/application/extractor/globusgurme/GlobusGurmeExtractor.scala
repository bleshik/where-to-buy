package wh.application.extractor.globusgurme

import java.net.URL
import wh.application.extractor.Extract
import wh.application.extractor.ExtractCategory
import wh.application.extractor.ExtractRegion
import wh.application.extractor.{AbstractJsoupExtractor, JsoupPage}
import wh.extractor.domain.model.{Category, ExtractedEntry}

import scala.collection.JavaConverters._
import wh.application.extractor.JsoupPage._

class GlobusGurmeExtractor extends AbstractJsoupExtractor {

  protected def when(e: Extract): Unit = Map(
    "Москва" -> "%CC%EE%F1%EA%E2%E0",
    "Санкт-Петербург" -> "%D1%E0%ED%EA%F2-%CF%E5%F2%E5%F0%E1%F3%F0%E3"
  ).foreach { city =>
    sendToMyself(ExtractRegion(city._1, Extract(new URL(e.url.toString + "?city=" + city._2), e.callback)))
  }

  protected def when(e: ExtractRegion): Unit =
    extractFromCategories(e, document(e), None)

  protected def when(e: ExtractCategory): Unit =
    extractFromCategories(e.extractRegion, document(e), Some(e.category))

  private def extractFromCategories(e: ExtractRegion, page: Option[JsoupPage], category: Option[Category]): Unit = {
    page.map { page: JsoupPage =>
      if (category.exists(_.name.toLowerCase.trim.contains("подарочные наборы"))) {
        return
      }
      val categories = page.document.select("li.hide640 a")
      if (categories.isEmpty) {
        category.map { c =>
          page.document.select("ul.gg-search-pager")
            .asScala
            .headOption
            .map { pager =>
              pager.select("a").asScala.flatMap(url(_, "href")).map { url =>
                extractEntries(ExtractCategory(c, e.withUrl(url)), document(e.withUrl(url)))
              }
            }
            extractEntries(ExtractCategory(c, e.withUrl(page.url)), Some(page))
        }
      } else {
        categories.asScala.flatMap { categoryLink =>
          JsoupPage.url(categoryLink, "href").map { nextPage =>
            sendToMyself(ExtractCategory(Category(cleanUpName(categoryLink.ownText), category.getOrElse(null)), e.withUrl(nextPage)))
          }
        }
      }
    }
  }

  private def extractEntries(e: ExtractCategory, page: Option[JsoupPage]): Unit = {
    page.map { page: JsoupPage =>
      e.extractRegion.extract.callback(page.document.select("div.product-list-item")
        .asScala
        .flatMap { item =>
        item.select("div.price-item")
          .asScala
          .flatMap { priceDiv => extractPrice(priceDiv.text)
            val img = item.select("img")
            val name = cleanUpName(item.select("div.product-list-head a").text)
            val quantity =
              if (priceDiv.hasClass("price-item-weight"))
                "1 кг"
              else if (priceDiv.hasClass("price-item-items"))
                "1 шт"
              else
                ""
            extractEntry(
              "Глобус Гурмэ",
              e.extractRegion.region,
              name + " " + quantity,
              extractPrice(priceDiv.text),
              e.category,
              img
            )
          }
      })
    }
  }

  override protected def filterImage(image: URL): Boolean =
    super.filterImage(image) && !image.toString.toLowerCase.contains("food158_158_thumb")

}
