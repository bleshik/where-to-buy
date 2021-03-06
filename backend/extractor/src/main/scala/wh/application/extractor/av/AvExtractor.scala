package wh.application.extractor.av

import wh.application.extractor.Extract
import wh.application.extractor.ExtractCategory
import wh.application.extractor.ExtractRegion
import wh.application.extractor.{AbstractJsoupExtractor, JsoupPage, SupportedCity}
import wh.extractor.domain.model.{Category, ExtractedEntry}
import scala.util.Try

import scala.collection.JavaConverters._
import wh.application.extractor.JsoupPage._

class AvExtractor extends AbstractJsoupExtractor {

  protected def when(e: Extract): Unit = {
    extractFromFlatMenu(e)
    extractFromNestedMenu(e)
  }

  protected def when(e: ExtractCategory): Unit = {
    document(e).map { page =>
      e.extractRegion.extract.callback(page.document.select(".category_page_catalog_item_plate")
        .asScala
        .flatMap { item =>
        item.select(".item_price")
          .asScala
          .headOption
          .map { price => extractPrice(price.ownText + "," + price.select(".price_postfix").text) }
          .flatMap { price =>
            extractEntry(
              "Азбука Вкуса",
              e.extractRegion.region,
              item.select(".item_title").text,
              price,
              e.category,
              item.select("img")
            )
          }
        })
      page.document.select(".pagination_item.right a").asScala.foreach { link =>
        JsoupPage.url(link, "href").map { url => sendToMyself(e.withUrl(url)) }
      }
    }
  }

  private def extractFromFlatMenu(e: Extract): Unit = {
    document(e).map { page =>
      page.document.select(".main_page_categories")
        .asScala
        .headOption
        .map {
          _.select("a.category_item_link")
          .asScala
          .foreach { subCategory =>
            JsoupPage.url(subCategory, "href").map { url =>
              handle(Try(sendToMyself(
                ExtractCategory(Category(cleanUpName(subCategory.text), null), ExtractRegion("Москва", e)).withUrl(url)
              )))
            }
          }
        }
    }
  }

  private def extractFromNestedMenu(e: Extract): Unit = {
    document(e).map { page =>
      page.document.select(".categories_page_category_list .categories_page_item")
        .asScala
        .foreach { category =>
        val parent = Category(cleanUpName(category.select(".categories_page_item_left").text))
        category.select(".categories_page_item_right a")
          .asScala
          .foreach { subCategory =>
            JsoupPage.url(subCategory, "href").map { url =>
              handle(Try(sendToMyself(
                ExtractCategory(Category(cleanUpName(subCategory.text), parent), ExtractRegion("Москва", e)).withUrl(url)
              )))
            }
        }
      }
    }
  }

}
