package wh.application.extractor.dixy

import java.net.URL
import wh.application.extractor.Extract
import wh.application.extractor.ExtractCategory
import wh.application.extractor.ExtractCity
import wh.application.extractor.{JsoupPage, AbstractJsoupExtractor}
import wh.extractor.domain.model.ExtractedEntry

import scala.collection.JavaConverters._
import wh.application.extractor.JsoupPage._

class DixyExtractor extends AbstractJsoupExtractor {

  val regionToCity = Map(
    ("Архангельская область", "Архангельск"),
    ("Брянская область", "Брянск"),
    ("Владимирская область", "Владимир"),
    ("Вологодская область", "Вологда"),
    ("Ивановская область", "Иваново"),
    ("Калужская область", "Калуга"),
    ("Костромская область", "Кострома"),
    ("Ленинградская область", "Ленинград"),
    ("Москва", "Москва"),
    ("Московская область", "Москва"),
    ("Мурманская область", "Мурманск"),
    ("Нижегородская область", "Нижний Новгород"),
    ("Новгородская область", "Новгород"),
    ("Псковская область", "Псков"),
    ("Республика Карелия", "Республика Карелия"),
    ("Рязанская область", "Рязань"),
    ("Санкт-Петербург", "Санкт-Петербург"),
    ("Свердловская область", "Свердлов"),
    ("Смоленская область", "Смоленск"),
    ("Тверская область", "Тверь"),
    ("Тульская область", "Тула"),
    ("Челябинская область", "Челябинск"),
    ("Ярославская область", "Ярославль")
  )

  val dixyRegion = "dixy_region"

  override protected def when(e: Extract): Unit =
    regionToCity.map { region => sendToMyself(ExtractCity(region._2, e, Map((dixyRegion, region._1)))) }

  protected def when(e: ExtractCity): Unit = {
    document(e).map { page =>
      page.document.select("div#flowpanes div.fp-item")
        .asScala
        .foreach { item =>
          extractEntry(
            "Дикси",
            e.city,
            item.select("div.prices-3")
              .text
              .replace("весовые", "")
              .replace("весовая", "")
              .replace("весовой", ""),
            extractPrice(item.select(".price_now_n")
              .asScala
              .headOption
              .map(_.text)
              .getOrElse(""), 1),
            null,
            item.select("img")
          ).map { entry => e.extract.callback(entry) }
        }
    }
  }
}
