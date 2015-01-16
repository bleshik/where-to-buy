package wh.extractor.utkonos

import com.gargoylesoftware.htmlunit.html.{HtmlAnchor, HtmlDivision, HtmlInput, HtmlPage}
import wh.extractor.{AbstractHtmlUnitExtractor, Category, Entry}

import scala.collection.JavaConverters._
import scala.collection.mutable

class UtkonosExtractor extends AbstractHtmlUnitExtractor {
  override def doExtract(page: HtmlPage): Iterator[Entry] = {
    extractFromCategory(page, null)
  }

  private def extractFromCategory(page: HtmlPage, category: Category): Iterator[Entry] = {
    page.getBody.getElementsByAttribute("div", "class", "goods_container goods_view_box").asScala.headOption.asInstanceOf[Option[HtmlDivision]].map { goods =>
      goods.getElementsByAttribute("div", "class", "goods_view").asScala.asInstanceOf[mutable.Buffer[HtmlDivision]].map { entry =>
        Entry("Utkonos", cleanUpName(entry.getElementsByTagName("a").asScala.head.getTextContent),
          (BigDecimal(entry.getOneHtmlElementByAttribute("input", "name", "price").asInstanceOf[HtmlInput].getValueAttribute) * 100).toLong,
          category)
      }.iterator ++ page.getBody.getElementsByAttribute("div", "class", "el_paginate").asScala.headOption.asInstanceOf[Option[HtmlDivision]].map { pagination =>
        pagination.getElementsByTagName("a").asScala.lastOption.asInstanceOf[Option[HtmlAnchor]].map { lastLink =>
          if (cleanUpName(lastLink.getTextContent).equals("Вперед")) extractFromCategory(lastLink.click(), category) else Iterator.empty
        }.getOrElse(Iterator.empty)
      }.getOrElse(Iterator.empty)
    }.getOrElse(Iterator.empty) ++ page.getBody
      .getElementsByAttribute("a", "class", "align_center cat_preview")
      .asScala.asInstanceOf[mutable.Buffer[HtmlAnchor]]
      .iterator
      .flatMap { a =>
        extractFromCategory(a.click(), Category(cleanUpName(a.getChildNodes.get(1).getTextContent), category))
      }
  }
}
