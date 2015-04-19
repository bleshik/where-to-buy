package wh.application.extractor.av

import wh.application.extractor.{AbstractJsoupExtractor, JsoupPage, SupportedCity}
import wh.extractor.domain.model.{Category, ExtractedEntry}

import scala.collection.JavaConverters._

class AvExtractor extends AbstractJsoupExtractor {
  override def doExtract(page: JsoupPage): Iterator[ExtractedEntry] = {
    extractFromFlatMenu(page) ++ extractFromNestedMenu(page)
  }

  private def extractFromCategory(page: JsoupPage, category: Category): Iterator[ExtractedEntry] = {
    page.document.select(".category_page_catalog_item_plate")
      .asScala
      .iterator
      .flatMap { item =>
      item.select(".item_price")
        .asScala
        .headOption
        .map { price => extractPrice(price.ownText + "," + price.select(".price_postfix").text) }
        .flatMap { price =>
        extractEntry(
          "Азбука Вкуса",
          SupportedCity.Moscow.name,
          item.select(".item_title").text,
          price,
          category,
          item.select("img")
        )
      }
    } ++
    page.document.select(".pagination_item.right a").asScala.iterator.flatMap { link =>
      page.click(link).map { nextPage => extractFromCategory(nextPage, category) }.getOrElse(Iterator.empty)
    }
  }

  private def extractFromFlatMenu(page: JsoupPage): Iterator[ExtractedEntry] =
    page.document.select(".main_page_categories")
      .asScala
      .headOption
      .map {
      _.select("a.category_item_link")
        .asScala
        .iterator
        .flatMap { link =>
        page.click(link).map { nextPage =>
          extractFromCategory(nextPage, Category(cleanUpName(link.text)))
        }.getOrElse(Iterator.empty)
      }
    }.getOrElse(Iterator.empty)

  private def extractFromNestedMenu(page: JsoupPage): Iterator[ExtractedEntry] = {
    page.document.select(".categories_page_category_list .categories_page_item")
      .asScala
      .iterator
      .flatMap { category =>
      val parent = Category(cleanUpName(category.select(".categories_page_item_left").text))
      category.select(".categories_page_item_right a")
        .asScala
        .flatMap { subCategory =>
        page.click(subCategory).map { nextPage =>
          extractFromCategory(nextPage, Category(cleanUpName(subCategory.text), parent))
        }.getOrElse(Iterator.empty)
      }
    }
  }
}
