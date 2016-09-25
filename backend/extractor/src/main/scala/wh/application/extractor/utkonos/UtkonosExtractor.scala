package wh.application.extractor.utkonos

import java.net.URL
import wh.application.extractor.Extract
import wh.application.extractor.ExtractCategory
import wh.application.extractor.ExtractRegion
import wh.application.extractor.{JsoupPage, AbstractJsoupExtractor, SupportedCity}
import wh.extractor.domain.model.{Category, ExtractedEntry}
import scala.collection.JavaConverters._
import wh.application.extractor.JsoupPage._
import scala.util.Try

class UtkonosExtractor extends AbstractJsoupExtractor {

  protected def when(e: Extract): Unit =
    document(e).map(extractFromCategory(_, None, ExtractRegion("Москва", e)))

  protected def when(e: ExtractCategory): Unit =
    document(e).map(extractFromCategory(_, Some(e.category), e.extractRegion))

  private def extractFromCategory(page: JsoupPage, category: Option[Category], extractRegion: ExtractRegion): Unit = {

    page.document.select("div.goods_container.goods_view_box").asScala.headOption.map { goods =>

      extractRegion.extract.callback(goods.select("div.goods_view").asScala.flatMap { entry =>
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
          category.flatMap { c =>
            extractEntry("Утконос", "Москва", entry.select("a").first.text, price, c, entry.select("img"))
          }
        }
      })

      page.document.select("div.el_paginate").asScala.headOption.map { pagination =>
        pagination.select("a").asScala.lastOption.map { lastLink =>
          if (cleanUpName(lastLink.text).equals("Вперед"))
            url(lastLink).flatMap(document(_)).map(extractFromCategory(_, category, extractRegion))
        }
      }

    }

    page.document.select("a.align_center.cat_preview")
      .asScala
      .foreach { a =>
        url(a).map( url =>
          handle(Try(
            sendToMyself(
                ExtractCategory(Category(cleanUpName(a.text), category.getOrElse(null)), extractRegion).withUrl(url)
            )
          ))
        )
      }

  }

  override def filterImage(image: URL): Boolean = !image.toString.contains("empty.gif")
}
