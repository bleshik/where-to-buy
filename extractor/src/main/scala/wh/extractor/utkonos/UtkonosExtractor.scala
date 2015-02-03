package wh.extractor.utkonos

import com.gargoylesoftware.htmlunit.html.{HtmlAnchor, HtmlDivision, HtmlInput, HtmlPage}
import wh.extractor.{AbstractHtmlUnitExtractor, Category, ExtractedEntry}

import scala.collection.JavaConverters._
import scala.collection.mutable

class UtkonosExtractor(override val downloadImages: Boolean = true) extends AbstractHtmlUnitExtractor(downloadImages) {
  override def doExtract(page: HtmlPage): Iterator[ExtractedEntry] = {
    extractFromCategory(page, null)
  }

  private def extractFromCategory(page: HtmlPage, category: Category): Iterator[ExtractedEntry] = {
    page.getBody.getElementsByAttribute("div", "class", "goods_container goods_view_box").asScala.headOption.asInstanceOf[Option[HtmlDivision]].map { goods =>
      goods.getElementsByAttribute("div", "class", "goods_view").asScala.asInstanceOf[mutable.Buffer[HtmlDivision]].map { entry =>
        extractEntry(cleanUpName(entry.getElementsByTagName("a").asScala.head.getTextContent),
          (BigDecimal(entry.getOneHtmlElementByAttribute("input", "name", "price").asInstanceOf[HtmlInput].getValueAttribute) * 100).toLong,
          category,
          entry.getElementsByTagName("img").asScala.headOption.flatMap(download))
      }.iterator ++ page.getBody.getElementsByAttribute("div", "class", "el_paginate").asScala.headOption.asInstanceOf[Option[HtmlDivision]].map { pagination =>
        pagination.getElementsByTagName("a").asScala.lastOption.asInstanceOf[Option[HtmlAnchor]].map { lastLink =>
          if (cleanUpName(lastLink.getTextContent).equals("Вперед"))
            click(lastLink).map(extractFromCategory(_, category)).getOrElse(Iterator.empty)
          else
            Iterator.empty
        }.getOrElse(Iterator.empty)
      }.getOrElse(Iterator.empty)
    }.getOrElse(Iterator.empty) ++ page.getBody
      .getElementsByAttribute("a", "class", "align_center cat_preview")
      .asScala.asInstanceOf[mutable.Buffer[HtmlAnchor]]
      .iterator
      .flatMap { a =>
        click(a).map(extractFromCategory(_, Category(cleanUpName(a.getChildNodes.get(1).getTextContent), category))).getOrElse(Iterator.empty)
      }
  }
}
