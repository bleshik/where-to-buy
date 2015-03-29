package wh.application.extractor

import java.net.URL

import com.gargoylesoftware.htmlunit.html._
import wh.extractor.domain.model.{Category, ExtractedEntry}

import scala.collection.JavaConverters._
import scala.collection.mutable

class GlobusGurmeExtractor extends AbstractExtractor {
  override def extract(url: URL): Iterator[ExtractedEntry] = {
    Iterator("%CC%EE%F1%EA%E2%E0", "%D1%E0%ED%EA%F2-%CF%E5%F2%E5%F0%E1%F3%F0%E3").flatMap { city =>
      super.extract(new URL(url.toString + "?city=" + city))
    }
  }

  override def doExtract(page: HtmlPage): Iterator[ExtractedEntry] = extractEntriesFromCategories(page, null)

  private def extractEntriesFromCategories(page: HtmlPage, category: Category): Iterator[ExtractedEntry] = {
    val categories = page.getBody
      .getElementsByAttribute("li", "class", "hide640")
      .asScala
      .asInstanceOf[mutable.Buffer[HtmlListItem]]
      .toList
      .flatMap { li =>
      li.getElementsByTagName("a")
        .asScala
        .asInstanceOf[mutable.Buffer[HtmlAnchor]]
        .toList
    }
    if (categories.isEmpty) {
      extractEntriesFromCategory(page, category)
    } else {
      categories.toStream.iterator.flatMap { categoryLink =>
        click(categoryLink).map { nextPage =>
          extractEntriesFromCategories(nextPage, Category(cleanUpName(categoryLink.getFirstChild.getTextContent), category))
        }
      }.flatten
    }
  }

  private def extractEntriesFromCategory(page: HtmlPage, category: Category): Iterator[ExtractedEntry] = {
    val city = cleanUpName(page.getBody.getOneHtmlElementByAttribute("a", "title", "Выберите город").asInstanceOf[HtmlElement].getTextContent)
    (Iterator(page) ++
      page.getBody
        .getElementsByAttribute("ul", "class", "gg-search-pager")
        .asScala
        .asInstanceOf[mutable.Buffer[HtmlElement]]
        .flatMap { pages =>
        pages.getElementsByTagName("a").asScala.asInstanceOf[mutable.Buffer[HtmlAnchor]].map(href)
      }.flatten.toStream.flatMap(htmlPage(_))).flatMap { page: HtmlPage =>
      List("product-list-item product-list-item_first", "product-list-item ").flatMap { itemClass =>
        page.getBody
          .getElementsByAttribute("div", "class", itemClass)
          .asScala
          .asInstanceOf[mutable.Buffer[HtmlDivision]]
      }.flatMap { item =>
        List("price-item  price-item-items", "price-item price-item-weight", "price-item ").flatMap { priceClass =>
          item.getElementsByAttribute("div", "class", priceClass)
            .asScala
            .asInstanceOf[mutable.Buffer[HtmlDivision]]
            .headOption
            .map { priceDiv => extractPrice(priceDiv.getTextContent)}
            .map { price =>
            val img = item.getElementsByTagName("img").get(0).asInstanceOf[HtmlImage]
            val name = cleanUpName(item.getOneHtmlElementByAttribute("div", "class", "product-list-head")
              .asInstanceOf[HtmlDivision]
              .getElementsByTagName("a")
              .get(0)
              .asInstanceOf[HtmlAnchor]
              .getTextContent)
            val quantity =
              if (priceClass.equals("price-item price-item-weight"))
                "1 кг"
              else if (priceClass.equals("price-item  price-item-items"))
                "1 шт"
              else
                ""
            extractEntry("Глобус Гурмэ", city, name + " " + quantity, price, category, img)
          }
        }
      }.flatten.iterator
    }
  }
}
