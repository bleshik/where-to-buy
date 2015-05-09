package wh.application.extractor.utkonos

import java.net.URL

import wh.application.extractor.{JsoupPage, AbstractJsoupExtractor, SupportedCity}
import wh.extractor.domain.model.{Category, ExtractedEntry}

import scala.collection.JavaConverters._

class UtkonosExtractor extends AbstractJsoupExtractor {
  override def doExtract(page: JsoupPage): Iterator[ExtractedEntry] = {
    extractFromCategory(page, null)
  }

  private def extractFromCategory(page: JsoupPage, category: Category): Iterator[ExtractedEntry] = {
    page.document.select("div.goods_container.goods_view_box").asScala.headOption.map { goods =>
      goods.select("div.goods_view").asScala.flatMap { entry =>
        entry.select("div.price.color_black")
          .asScala
          .headOption
          .map(price => price.text)
          .orElse(
            entry.select("div.price.color_action")
              .asScala
              .headOption
              .map(_.ownText)
          ).map(extractPrice(_))
        .flatMap { price =>
          extractEntry(
            "Утконос",
            SupportedCity.Moscow.name,
            entry.select("a").first.text,
            price,
            category,
            entry.select("img"))
        }
      }.iterator ++ page.document.select("div.el_paginate").asScala.headOption.map { pagination =>
        pagination.select("a").asScala.lastOption.map { lastLink =>
          if (cleanUpName(lastLink.text).equals("Вперед"))
            page.click(lastLink).map(extractFromCategory(_, category)).getOrElse(Iterator.empty)
          else
            Iterator.empty
        }.getOrElse(Iterator.empty)
      }.getOrElse(Iterator.empty)
    }.getOrElse(Iterator.empty) ++ page.document.select("a.align_center.cat_preview")
      .asScala
      .iterator
      .flatMap { a =>
        page.click(a).map(extractFromCategory(_, Category(cleanUpName(a.text), category))).getOrElse(Iterator.empty)
      }
  }

  override def filterImage(image: URL): Boolean = !image.toString.contains("empty.gif")
}
