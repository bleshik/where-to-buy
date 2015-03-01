package wh.application.extractor.metro

import java.net.URL

import com.gargoylesoftware.htmlunit.html._
import wh.application.extractor.AbstractExtractor
import wh.extractor.domain.model.{Category, ExtractedEntry}

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.util.Try

class MetroExtractor extends AbstractExtractor {
  protected def domains(url: URL): Map[String, URL] = {
    json[List[Map[String, Object]]](url.toURI.resolve("/index.php?route=store/store/getstores").toURL)
      .map(_.flatMap(_.get("tradecenter").asInstanceOf[Option[List[Map[String, Object]]]].getOrElse(List.empty)))
      .map { stores =>
      stores.map { store =>
        (store.get("city_name").get.asInstanceOf[String], new URL("http://" + store.get("domain").get.asInstanceOf[String]))
      }.toMap
    }.getOrElse(Map())
  }

  override def doExtract(page: HtmlPage): Iterator[ExtractedEntry] = {
    domains(page.getUrl).toStream.iterator.flatMap { domain =>
      htmlPage(domain._2).map{page =>
        page.getBody
          .getElementsByAttribute("li", "class", "item __submenu")
          .asScala
          .asInstanceOf[mutable.Buffer[HtmlListItem]]
          .toStream
          .iterator
          .flatMap { submenu =>
          val rootCategory = Category(cleanUpName(submenu.getFirstChild.getTextContent), null)
          submenu.getElementsByAttribute("div", "class", "subcatalog_list")
            .asScala
            .asInstanceOf[mutable.Buffer[HtmlDivision]]
            .toStream
            .iterator
            .flatMap { subcatalog =>
            val category = Category(cleanUpName(subcatalog.getFirstChild.getTextContent), rootCategory)
            subcatalog.getElementsByAttribute("a", "class", "subcatalog_link")
              .asScala
              .asInstanceOf[mutable.Buffer[HtmlAnchor]]
              .toStream
              .iterator
              .flatMap { subcatalogLink =>
              href(subcatalogLink).map { href =>
                val subCategory = Category(cleanUpName(subcatalogLink.getTextContent), category)
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
      json.map(_.get("data").asInstanceOf[Option[Map[String, Object]]])
        .flatten
        .map(_.get("items").asInstanceOf[Option[List[String]]])
        .flatten.map { entries =>
        entries.iterator.map(html).flatMap { entryPage =>
          entryPage.getBody
            .getElementsByAttribute("div", "class", "current")
            .asScala
            .asInstanceOf[mutable.Buffer[HtmlDivision]]
            .headOption
            .map { price =>
            val img = entryPage.getBody.getElementsByTagName("img").get(0)
            extractEntry(
              "Metro",
              city,
              img.getAttribute("title"),
              extractPrice(price.getTextContent, 1),
              category,
              img
            )
          }
        }.flatten
      }.getOrElse(Iterator.empty)
    }
  }
}
