package wh.application.extractor.metro

import java.net.URL

import wh.application.extractor.JsoupPage._
import wh.application.extractor.{JsoupPage, AbstractJsoupExtractor}
import wh.extractor.domain.model.{Category, ExtractedEntry}

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

  override def doExtract(page: JsoupPage): Iterator[ExtractedEntry] = {
    domains(new URL(page.document.baseUri)).toStream.iterator.flatMap { domain =>
      document(domain._2).map{ page =>
        page.document.select("li.item.__submenu")
          .asScala
          .iterator
          .filterNot(_.classNames().contains("__action"))
          .flatMap { submenu =>
          val rootCategory = Category(cleanUpName(submenu.child(0).text), null)
          submenu.select("div.subcatalog_list")
            .asScala
            .iterator
            .flatMap { subcatalog =>
            val category = Category(cleanUpName(subcatalog.child(0).text), rootCategory)
            subcatalog.select("a.subcatalog_link")
              .asScala
              .iterator
              .flatMap { subcatalogLink =>
              url(subcatalogLink, "href").map { href =>
                val subCategory = Category(cleanUpName(subcatalogLink.text), category)
                handle(Try(extractCategoryEntries(domain._1, href, subCategory))).getOrElse(Iterator.empty)
              }.getOrElse(Iterator.empty)
            }
          }
        }
      }.getOrElse(Iterator.empty)
    }
  }

  protected def entriesUrls(categoryUrl: URL): List[URL] = {
    1.to(50).map(page => new URL(categoryUrl.toString + s"?price_from=0&price_to=300000&brands=&attrs=&sorting=0&limit=20&page=$page&format=j")).toList
  }

  private def extractCategoryEntries(city: String, categoryUrl: URL, category: Category): Iterator[ExtractedEntry] = {
    entriesUrls(categoryUrl)
      .toStream
      .map { url => json[Map[String, Object]](url) }
      .takeWhile { json =>
        json.isDefined && !json.get.get("data").asInstanceOf[Option[Map[String, Object]]].exists { d =>
          d.get("items").asInstanceOf[Option[List[String]]].exists(_.isEmpty)
        }
      }.iterator
      .flatMap { json =>
      json.flatMap(_.get("data").asInstanceOf[Option[Map[String, Object]]])
        .flatMap(_.get("items").asInstanceOf[Option[List[String]]])
        .map { entries =>
        entries.iterator.map(document).flatMap { entryPage =>
          entryPage.document.select("div.current")
            .asScala
            .headOption
            .map { price =>
            val img = entryPage.document.select("img").first
            extractEntry(
              "Metro",
              city,
              img.attr("title"),
              extractPrice(price.text, 1),
              category,
              img
            )
          }
        }.flatten
      }.getOrElse(Iterator.empty)
    }
  }
}
