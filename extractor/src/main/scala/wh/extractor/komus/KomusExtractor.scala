package wh.extractor.komus

import com.gargoylesoftware.htmlunit.html.{HtmlAnchor, HtmlDivision, HtmlPage, HtmlSpan}
import wh.extractor.{AbstractHtmlUnitExtractor, Category, ExtractedEntry}

import scala.collection.JavaConverters._
import scala.collection.mutable

class KomusExtractor extends AbstractHtmlUnitExtractor {

  override def doExtract(page: HtmlPage): Iterator[ExtractedEntry] = extractFromCategoryList(page, null)

  private def extractFromCategoryList(page: HtmlPage, parentCategory: Category): Iterator[ExtractedEntry] = {
    page.getBody.getElementsByAttribute("a", "title", "подробно")
      .asScala
      .headOption
      .asInstanceOf[Option[HtmlAnchor]]
      .map(a => click(a).map(extractFromCategoryList(_, parentCategory)).getOrElse(Iterator.empty))
      .getOrElse(
        extractFromProductList(page, parentCategory) ++
          page.getBody
            .getElementsByAttribute("a", "class", "pagination--item-page-link")
            .asScala
            .asInstanceOf[mutable.Buffer[HtmlAnchor]]
            .flatMap(a => click(a).map(extractFromProductList(_, parentCategory)).getOrElse(Iterator.empty)) ++
          page.getBody
          .getElementsByAttribute("a", "class","catalog--ag-head-link")
          .asInstanceOf[java.util.List[HtmlAnchor]]
          .asScala
          .iterator
          .flatMap { a =>
              click(a).map(extractFromCategoryList(_, Category(cleanUpName(a.getChildNodes.get(0).getTextContent), parentCategory))).getOrElse(Iterator.empty)
          }
      )
  }

  private def extractFromProductList(page: HtmlPage, category: Category): Iterator[ExtractedEntry] = {
    List("full", "table").flatMap { diff =>
      val entries = page.getBody.getElementsByAttribute("div", "class", "goods-" + diff + "--info").asInstanceOf[java.util.List[HtmlDivision]].asScala
      entries.map { entry =>
        val name = cleanUpName(entry.getOneHtmlElementByAttribute("a", "class", "goods-" + diff + "--name-link").asInstanceOf[HtmlAnchor].getChildNodes.get(0).getTextContent)
        val stringPrice = cleanUpName(entry.getOneHtmlElementByAttribute("span", "class", "goods-" + diff +  "--price-now-value").asInstanceOf[HtmlSpan].getChildNodes.get(0).getTextContent)
        val price = (BigDecimal(stringPrice.replace(',', '.').replace(" ", "")) * 100).toLong
        extractEntry(name, price, category)
      }
    }.iterator
  }
}
