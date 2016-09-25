package wh.application.extractor.dixy

import java.net.URL
import wh.application.extractor.Extract
import wh.application.extractor.ExtractRegion
import wh.application.extractor.{JsoupPage, AbstractJsoupExtractor}
import wh.extractor.domain.model.ExtractedEntry
import wh.extractor.domain.model.Category

import scala.collection.JavaConverters._
import wh.application.extractor.JsoupPage._

class DixyExtractor extends AbstractJsoupExtractor {

  val regions = Set(
    "Архангельская область",
    "Брянская область",
    "Владимирская область",
    "Вологодская область",
    "Ивановская область",
    "Калужская область",
    "Костромская область",
    "Ленинградская область",
    "Москва",
    "Московская область",
    "Мурманская область",
    "Нижегородская область",
    "Новгородская область",
    "Псковская область",
    "Республика Карелия",
    "Рязанская область",
    "Санкт-Петербург",
    "Свердловская область",
    "Смоленская область",
    "Тверская область",
    "Тульская область",
    "Челябинская область",
    "Ярославская область"
  )

  val dixyRegion = "dixy_region"

  protected def when(e: Extract): Unit =
    regions.foreach { region => sendToMyself(ExtractRegion(region, e, Map((dixyRegion, region)))) }

  protected def when(e: ExtractRegion): Unit = {
    document(e).map { page =>
      e.extract.callback(page.document.select("div.product")
        .asScala
        .flatMap { item =>
          extractEntry(
            "Дикси",
            e.region,
            item.select("div.product-name")
              .text
              .replace("весовые", "")
              .replace("весовая", "")
              .replace("весовой", ""),
            extractPrice(item.select("div.price")
              .asScala
              .headOption
              .flatMap((p1) =>
                  item.select("div.fract").asScala.headOption.map((p2) => s"${p1.text}${p2.text}")
              ).getOrElse(""), 1),
            Category(cleanUpName(item.select("div.product-category").text)),
            item.select("img")
          )
        }
      )
    }
  }
}
