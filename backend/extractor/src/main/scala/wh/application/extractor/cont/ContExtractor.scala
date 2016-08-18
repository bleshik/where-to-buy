package wh.application.extractor.cont

import wh.application.extractor.Extract
import wh.application.extractor.ExtractCategory
import wh.application.extractor.ExtractCity
import wh.application.extractor.{JsoupPage, AbstractJsoupExtractor, SupportedCity}
import wh.extractor.domain.model.{Category, ExtractedEntry}

import scala.collection.JavaConverters._
import scala.util.Try
import wh.application.extractor.JsoupPage._

class ContExtractor extends AbstractJsoupExtractor {
  override protected def when(e: Extract): Unit = {
    document(e).map { page =>
      page.document.select("ul#categories a")
      .asScala
      .foreach(a =>
        JsoupPage.url(a, "href").map { url =>
          sendToMyself(ExtractCategory(Category(cleanUpName(a.text), null), ExtractCity("Москва", e)).withUrl(url))
        }
      )
    }
  }

  protected def when(e: ExtractCategory): Unit = {
    document(e).map { page =>
      val categories = page.document.select("div#goodscategories")
        .asScala
        .headOption
      if (categories.nonEmpty) {
        categories.map { c =>
          c.select("a")
            .asScala
            .foreach(cp =>
              JsoupPage.url(cp, "href").map { url =>
                sendToMyself(e.copy(category = Category(cleanUpName(cp.text), e.category)).withUrl(url))
              }
            )
        }
      } else {
        extractFromEntryList(page, e)
        page.document.select("div#pages a").asScala.foreach { a =>
          page.click(a).map { p =>
            extractFromEntryList(p, e)
          }
        }
      }
    }
  }

  private def extractFromEntryList(page: JsoupPage, e: ExtractCategory): Unit = {
    page.document.select("div.item")
      .asScala
      .foreach { item =>
        extractEntry(
          "Седьмой Континент",
          SupportedCity.Moscow.name,
          item.select("div.title a").text,
          extractPrice(item.select("div.currentprice").text, 1),
          e.category,
          item.select("img")
        ).map { entry => e.extractCity.extract.callback(entry) }
      }
  }
}
