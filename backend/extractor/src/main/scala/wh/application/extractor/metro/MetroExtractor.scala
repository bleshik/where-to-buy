package wh.application.extractor.metro

import java.net.URL
import java.util.concurrent.LinkedBlockingQueue
import wh.application.extractor.Extract
import wh.application.extractor.ExtractCategory
import wh.application.extractor.ExtractRegion
import wh.application.extractor.JsoupPage._
import wh.application.extractor.{JsoupPage, AbstractJsoupExtractor}
import wh.extractor.domain.model.{Category, ExtractedEntry}
import wh.util.WaitingBlockingQueueIterator
import scala.collection.JavaConverters._
import scala.util.Try

class MetroExtractor extends AbstractJsoupExtractor {
  protected def domains(url: URL): Map[String, URL] = {
    json[List[Map[String, Object]]](url.toURI.resolve("/index.php?route=store/store/getstores").toURL)
      .map(_.flatMap(_.get("tradecenter").asInstanceOf[Option[List[Map[String, Object]]]].getOrElse(List.empty)))
      .map { stores =>
      stores.map { store =>
        (store.get("city_name").get.asInstanceOf[String], new URL("http://" + store.get("domain").get.asInstanceOf[String]))
      }.toMap
    }.getOrElse(Map())
  }

  protected def when(e: Extract): Unit = 
    domains(e.url).foreach { d => sendToMyself(ExtractRegion(d._1, Extract(d._2, e.callback))) }

  protected def when(e: ExtractRegion): Unit = {
    document(e).foreach { page =>
      page.document.select("li.item.__submenu")
        .asScala
        .filterNot(_.classNames().contains("__action"))
        .flatMap { submenu =>
        val rootCategory = Category(cleanUpName(submenu.child(0).text), null)
        submenu.select("div.subcatalog_list")
          .asScala
          .flatMap { subcatalog =>
          val category = Category(cleanUpName(subcatalog.child(0).text), rootCategory)
          subcatalog.select("a.subcatalog_link")
            .asScala
            .flatMap { subcatalogLink =>
            url(subcatalogLink, "href").map { href =>
              ExtractCategory(
                Category(cleanUpName(subcatalogLink.text), category),
                ExtractRegion(e.region, Extract(href, e.extract.callback))
              )
            }
          }
        }
      }.foreach { extract => sendToMyself(extract) }
    }
  }

  protected def when(e: ExtractCategory): Unit =
    e.extractRegion.extract.callback(entriesUrls(e.extractRegion.extract.url)
    .map { url => json[Map[String, Object]](url) }
    .takeWhile { json =>
      json.isDefined && !json.get.get("data").asInstanceOf[Option[Map[String, Object]]].exists { d =>
        d.get("items").asInstanceOf[Option[List[String]]].exists(_.isEmpty)
      }
    }
    .flatMap { json =>
      json.flatMap(_.get("data").asInstanceOf[Option[Map[String, Object]]])
        .flatMap(_.get("items").asInstanceOf[Option[List[String]]])
        .map { entries =>
        entries.map(document).flatMap { entryPage =>
          entryPage.document.select("div.current")
            .asScala
            .headOption
            .flatMap { price =>
            val img = entryPage.document.select("img").first
            extractEntry(
              "Metro",
              e.extractRegion.region,
              img.attr("title"),
              extractPrice(price.text, 1),
              e.category,
              img
            )
          }
        }
      }
    }.flatten)

  protected def entriesUrls(categoryUrl: URL): List[URL] = {
    1.to(50).map(page => new URL(categoryUrl.toString + s"?price_from=0&price_to=300000&brands=&attrs=&sorting=0&limit=20&page=$page&format=j")).toList
  }

}
