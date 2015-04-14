package wh.application.extractor.cont

import wh.application.extractor.{JsoupPage, AbstractJsoupExtractor, SupportedCity}
import wh.extractor.domain.model.{Category, ExtractedEntry}

import scala.collection.JavaConverters._
import scala.util.Try

class ContExtractor extends AbstractJsoupExtractor {
  override def doExtract(page: JsoupPage): Iterator[ExtractedEntry] = {
    page.document.select("ul#categories a")
      .asScala
      .iterator
      .flatMap(a => page.click(a).flatMap(p => handle(Try(extractFromCategoryList(p, Category(cleanUpName(a.text), null))))).getOrElse(Iterator.empty))
  }

  private def extractFromCategoryList(page: JsoupPage, category: Category): Iterator[ExtractedEntry] = {
    page.document.select("div#goodscategories")
      .asScala
      .headOption
      .map { c =>
        c.select("a")
          .asScala
          .iterator
          .flatMap(cp => page.click(cp).flatMap(p => handle(Try(extractFromCategoryList(p, Category(cleanUpName(cp.text), category))))).getOrElse(Iterator.empty))
      }.getOrElse(
        extractFromEntryList(page, category)
        ++
        page.document.select("div#pages a").asScala.flatMap { a =>
          page.click(a).flatMap { p =>
            handle(Try(extractFromEntryList(p, category)))
          }.getOrElse(Iterator.empty)
        }
      )
  }

  private def extractFromEntryList(page: JsoupPage, category: Category): Iterator[ExtractedEntry] = {
    page.document.select("div.item")
      .asScala
      .iterator
      .flatMap { e =>
        extractEntry(
          "Седьмой Континент",
          SupportedCity.Moscow.name,
          e.select("div.title a").text,
          extractPrice(e.select("div.currentprice").text, 1),
          category,
          e.select("img")
        )
      }
  }
}
