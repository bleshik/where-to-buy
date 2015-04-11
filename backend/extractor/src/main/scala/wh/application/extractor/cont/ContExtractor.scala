package wh.application.extractor.cont

import org.jsoup.nodes.Document
import wh.application.extractor.{AbstractJsoupExtractor, SupportedCity}
import wh.extractor.domain.model.{Category, ExtractedEntry}

import scala.collection.JavaConverters._
import scala.util.Try

class ContExtractor extends AbstractJsoupExtractor {
  override def doExtract(page: Document): Iterator[ExtractedEntry] = {
    page.select("ul#categories a")
      .asScala
      .iterator
      .flatMap(a => click(a).flatMap(p => handle(Try(extractFromCategoryList(p, Category(cleanUpName(a.text), null))))).getOrElse(Iterator.empty))
  }

  private def extractFromCategoryList(page: Document, category: Category): Iterator[ExtractedEntry] = {
    page.select("div#goodscategories")
      .asScala
      .headOption
      .map { c =>
        c.select("a")
          .asScala
          .iterator
          .flatMap(cp => click(cp).flatMap(p => handle(Try(extractFromCategoryList(p, Category(cleanUpName(cp.text), category))))).getOrElse(Iterator.empty))
      }.getOrElse(
        extractFromEntryList(page, category)
        ++
        page.select("div#pages a").asScala.flatMap { a =>
          click(a).flatMap { p =>
            handle(Try(extractFromEntryList(p, category)))
          }.getOrElse(Iterator.empty)
        }
      )
  }

  private def extractFromEntryList(page: Document, category: Category): Iterator[ExtractedEntry] = {
    page.select("div.item")
      .asScala
      .iterator
      .flatMap { e =>
        extractEntry(
          "Седьмой Континент",
          SupportedCity.Moscow.name,
          e.select("div.title a").text,
          extractPrice(e.select("div.currentprice").text),
          category,
          e.select("img")
        )
      }
  }
}
