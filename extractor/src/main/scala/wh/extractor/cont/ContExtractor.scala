package wh.extractor.cont

import com.gargoylesoftware.htmlunit.html._
import wh.extractor.{AbstractHtmlUnitExtractor, Category, ExtractedEntry}

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.util.Try

class ContExtractor extends AbstractHtmlUnitExtractor {
  override def doExtract(page: HtmlPage): Iterator[ExtractedEntry] = {
    page.getBody
      .getOneHtmlElementByAttribute("ul", "id", "categories")
      .asInstanceOf[HtmlElement]
      .getElementsByTagName("a")
      .asScala
      .asInstanceOf[mutable.Buffer[HtmlAnchor]]
      .iterator
      .flatMap(a => click(a).map(p => handle(Try(extractFromCategoryList(p, Category(cleanUpName(a.getTextContent), null))))).getOrElse(Iterator.empty))
  }

  private def extractFromCategoryList(page: HtmlPage, category: Category): Iterator[ExtractedEntry] = {
    page.getBody
      .getElementsByAttribute("div", "id", "goodscategories")
      .asScala
      .headOption
      .asInstanceOf[Option[HtmlDivision]]
      .map { c =>
        c.getElementsByTagName("a")
          .asScala
          .asInstanceOf[mutable.Buffer[HtmlAnchor]]
          .iterator
          .flatMap(cp => click(cp).map(p => handle(Try(extractFromCategoryList(p, Category(cleanUpName(cp.getTextContent), category))))).getOrElse(Iterator.empty))
      }.getOrElse(
        extractFromEntryList(page, category)
        // pages are not handled properly for some reason
        /* ++
        page.getBody
          .getElementsByAttribute("div", "id", "pages")
          .asScala
          .asInstanceOf[mutable.Buffer[HtmlDivision]]
          .headOption
          .map(d => d.getElementsByTagName("a")
            .asScala
            .asInstanceOf[mutable.Buffer[HtmlAnchor]]
            .flatMap(a => click(a).map(p => handle(Try(extractFromEntryList(_, category)))).getOrElse(Iterator.empty))
          ).getOrElse(Iterator.empty)*/
      )
  }

  private def extractFromEntryList(page: HtmlPage, category: Category): Iterator[ExtractedEntry] = {
    page.getBody
      .getElementsByAttribute("div", "class", "item")
      .asScala
      .iterator
      .asInstanceOf[Iterator[HtmlDivision]]
      .flatMap { e =>
        extractEntry(
          cleanUpName(e.getOneHtmlElementByAttribute("div", "class", "title").asInstanceOf[HtmlDivision].getElementsByTagName("a").get(0).getTextContent),
          e.getOneHtmlElementByAttribute("div", "class", "currentprice").asInstanceOf[HtmlDivision].getTextContent.trim.toLong,
          category,
          e.getElementsByTagName("img").get(0)
        )
      }
  }
}
