package wh.application.extractor.dixy

import java.net.URL

import wh.application.extractor.{JsoupPage, AbstractJsoupExtractor}
import wh.extractor.domain.model.ExtractedEntry

import scala.collection.JavaConverters._

class DixyExtractor(val cities: Set[String] = null) extends AbstractJsoupExtractor {
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

  override def extract(url: URL): Iterator[ExtractedEntry] =
    regionToCity
      .filter(region => cities == null || cities.contains(region._1) || cities.contains(region._2))
      .iterator
      .flatMap { region =>
      super.extract(url, Map((dixyRegion, region._1)))
    }

  override def doExtract(page: JsoupPage): Iterator[ExtractedEntry] = {
    val region = page.document.select("select#switch-region option[selected]").text
    page.document.select("div#flowpanes div.fp-item")
      .asScala
      .flatMap { item =>
      regionToCity.get(region).flatMap { city =>
        extractEntry(
          "Дикси",
          city,
          item.select("div.prices-3")
            .text
            .replace("весовая", "")
            .replace("весовой", ""),
          extractPrice(item.select("h5.price-now.threedigit")
            .asScala
            .headOption
            .map(_.text)
            .getOrElse(item.select("h5.price-now").text)),
          null,
          item.select("img")
        )
      }
    }.iterator
  }
}
