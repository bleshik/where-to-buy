package wh.application.extractor.komus

import org.jsoup.nodes.Document
import wh.application.extractor.{AbstractJsoupExtractor, SupportedCity}
import wh.extractor.domain.model.{Category, ExtractedEntry}

import scala.collection.JavaConverters._

class KomusExtractor extends AbstractJsoupExtractor {

  override def doExtract(page: Document): Iterator[ExtractedEntry] = extractFromCategoryList(page, null)

  private def extractFromCategoryList(page: Document, parentCategory: Category): Iterator[ExtractedEntry] = {
    page.select("a[title=подробно]")
      .asScala
      .headOption
      .map(a => click(a).map(extractFromCategoryList(_, parentCategory)).getOrElse(Iterator.empty))
      .getOrElse(
        extractFromProductList(page, parentCategory) ++
          page.select("div.pagination")
            .asScala
            .headOption
            .map { pages =>
              pages.select("a.pagination--item-page-link")
              .asScala
              .flatMap(a => click(a).map(extractFromProductList(_, parentCategory)).getOrElse(Iterator.empty))
          }.getOrElse(Iterator.empty) ++
          page.select("a.catalog--ag-head-link")
          .asScala
          .iterator
          .flatMap { a =>
              click(a).map(extractFromCategoryList(_, Category(cleanUpName(a.ownText), parentCategory))).getOrElse(Iterator.empty)
          }
      )
  }

  private def extractFromProductList(page: Document, category: Category): Iterator[ExtractedEntry] = {
    List("full", "table").flatMap { diff =>
      val entries = page.select("div.goods-" + diff + "--inside").asScala
      entries.flatMap { entry =>
        val name = entry.select("a.goods-" + diff + "--name-link").text
        val price = extractPrice(entry.select("span.goods-" + diff +  "--price-now-value").text)
        val image = entry.select("img")
        extractEntry("Komus", SupportedCity.Moscow.name, name, price, category, image)
      }
    }.iterator
  }
}
