package wh.application.extractor

import java.net.URL

import org.jsoup.nodes.Document
import wh.extractor.domain.model.{Category, ExtractedEntry}

import scala.collection.JavaConverters._

class GlobusGurmeExtractor extends AbstractJsoupExtractor {
  override def extract(url: URL): Iterator[ExtractedEntry] = {
    Iterator("%CC%EE%F1%EA%E2%E0", "%D1%E0%ED%EA%F2-%CF%E5%F2%E5%F0%E1%F3%F0%E3").flatMap { city =>
      super.extract(new URL(url.toString + "?city=" + city))
    }
  }

  override def doExtract(page: Document): Iterator[ExtractedEntry] = extractEntriesFromCategories(page, null)

  private def extractEntriesFromCategories(page: Document, category: Category): Iterator[ExtractedEntry] = {
    val categories = page.select("li.hide640 a")
    if (categories.isEmpty) {
      extractEntriesFromCategory(page, category)
    } else {
      categories.asScala.iterator.flatMap { categoryLink =>
        click(categoryLink).map { nextPage =>
          extractEntriesFromCategories(nextPage, Category(cleanUpName(categoryLink.ownText), category))
        }
      }.flatten
    }
  }

  private def extractEntriesFromCategory(page: Document, category: Category): Iterator[ExtractedEntry] = {
    val city = cleanUpName(page.select("a[title=Выберите город]").text)
    page.select("ul.gg-search-pager")
      .asScala
      .headOption
      .map { Iterator(page) ++ _.select("a").asScala.flatMap(url(_, "href")).toStream.iterator.flatMap(document(_)) }
      .getOrElse(Iterator(page))
      .flatMap { page: Document =>
        page.select("div.product-list-item")
          .asScala
          .flatMap { item =>
          item.select("div.price-item")
            .asScala
            .flatMap { priceDiv => extractPrice(priceDiv.text)
              val img = item.select("img")
              val name = cleanUpName(item.select("div.product-list-head a").text)
              val quantity =
                if (priceDiv.hasClass("price-item-weight"))
                  "1 кг"
                else if (priceDiv.hasClass("price-item-items"))
                  "1 шт"
                else
                  ""
              extractEntry("Глобус Гурмэ", city, name + " " + quantity, extractPrice(priceDiv.text), category, img)
          }.iterator
        }
    }
  }
}
