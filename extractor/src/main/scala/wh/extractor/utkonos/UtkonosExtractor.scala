package wh.extractor.utkonos

import java.net.URL

import com.gargoylesoftware.htmlunit.html.{HtmlAnchor, HtmlDivision, HtmlInput, HtmlPage}
import wh.extractor.{AbstractHtmlUnitExtractor, Category, ExtractedEntry}

import scala.collection.JavaConverters._
import scala.collection.mutable

class UtkonosExtractor extends AbstractHtmlUnitExtractor {
  override def doExtract(page: HtmlPage): Iterator[ExtractedEntry] = {
    extractFromCategory(page, null)
  }

  private def extractFromCategory(page: HtmlPage, category: Category): Iterator[ExtractedEntry] = {
    page.getBody.getElementsByAttribute("div", "class", "goods_container goods_view_box").asScala.headOption.asInstanceOf[Option[HtmlDivision]].map { goods =>
      List("goods_view has_weight", "goods_view").flatMap(cl => goods.getElementsByAttribute("div", "class", cl).asScala.asInstanceOf[mutable.Buffer[HtmlDivision]].flatMap { entry =>
        extractEntry(
          "Утконос",
          cleanUpName(entry.getElementsByTagName("a").asScala.head.getTextContent),
          (BigDecimal(
            entry.getElementsByAttribute("div", "class", "price color_black")
              .asScala
              .headOption
              .asInstanceOf[Option[HtmlDivision]]
              .map(price => price.getChildNodes.get(0).getTextContent)
              .getOrElse(
                entry.getOneHtmlElementByAttribute("div", "class", "price color_action").asInstanceOf[HtmlDivision]
                  .getChildNodes.get(1)
                  .getTextContent
              ).replaceAll("\\s+", "").replace(",", ".")
          ) * 100).toLong,
          category,
          entry.getElementsByTagName("img").get(0))
      }).iterator ++ page.getBody.getElementsByAttribute("div", "class", "el_paginate").asScala.headOption.asInstanceOf[Option[HtmlDivision]].map { pagination =>
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

  override def filterImage(image: URL): Boolean = !image.toString.contains("empty.gif")
}
