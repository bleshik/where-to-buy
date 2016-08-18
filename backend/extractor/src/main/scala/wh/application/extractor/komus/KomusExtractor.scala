package wh.application.extractor.komus

import wh.application.extractor.Extract
import wh.application.extractor.ExtractCategory
import wh.application.extractor.ExtractCity
import wh.application.extractor.{JsoupPage, AbstractJsoupExtractor, SupportedCity}
import wh.extractor.domain.model.{Category, ExtractedEntry}
import scala.collection.JavaConverters._
import wh.application.extractor.JsoupPage._

class KomusExtractor extends AbstractJsoupExtractor {

  override protected def when(e: Extract): Unit =
    document(e).map { page => extractFromCategoryList(page, None, ExtractCity("Москва", e)) }

  protected def when(e: ExtractCategory): Unit =
    document(e).map { page => extractFromCategoryList(page, Some(e.category), e.extractCity) }

  private def getCategories(page: JsoupPage, parentCategory: Option[Category], extractCity: ExtractCity): Set[ExtractCategory] =
    page.document.select("div.b-account__item a")
    .asScala
    .flatMap { a =>
      JsoupPage.url(a).map { url =>
        ExtractCategory(Category(cleanUpName(a.ownText), parentCategory.getOrElse(null)), extractCity.withUrl(url))
      }
    }.toSet

  private def extractFromCategoryList(page: JsoupPage, parentCategory: Option[Category], extractCity: ExtractCity): Unit = {
    val categories = getCategories(page, parentCategory, extractCity)
    if (categories.nonEmpty) {
      categories.foreach(sendToMyself(_))
    } else {
      page.document.select("ul.b-pageNumber")
        .asScala
        .headOption
        .map { pagination =>
          pagination.select("a.b-pageNumber__item").asScala.flatMap { link => JsoupPage.url(link) }.toSet.iterator
        }
        .getOrElse(Iterator(page.url))
        .foreach { url =>
          (if (page.url.equals(url)) Some(page) else document(url)).map { page =>
            extractFromProductList(page, parentCategory).foreach { entry =>
              extractCity.extract.callback(entry)
            }
          }
        }
    }
  }

  private def extractFromProductList(page: JsoupPage, category: Option[Category]): Iterator[ExtractedEntry] = {
    category.map { c =>
      val entries = page.document.select("li.b-productList__item").asScala
      entries.iterator.flatMap { entry =>
        val name = entry.select("a.b-productList__item__descr--title").text
        val price = extractPrice(entry.select("span.b-price").text)
        val image = entry.select("img")
        extractEntry("Komus", SupportedCity.Moscow.name, name, price, c, image)
      }
    }.getOrElse(Iterator.empty)
  }
}
