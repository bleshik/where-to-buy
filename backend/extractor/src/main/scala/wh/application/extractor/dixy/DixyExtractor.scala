package wh.application.extractor.dixy

import java.net.{URLDecoder, URL}
import java.nio.charset.StandardCharsets

import com.gargoylesoftware.htmlunit.html.{HtmlDivision, HtmlHeading5, HtmlPage}
import wh.application.extractor.AbstractExtractor
import wh.extractor.domain.model.ExtractedEntry

import scala.collection.JavaConverters._
import scala.collection.mutable

class DixyExtractor(val cities: Set[String] = null) extends AbstractExtractor {
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

  override def doExtract(page: HtmlPage): Iterator[ExtractedEntry] = {
    val region = page.getWebClient.getCookieManager.getCookie(dixyRegion).getValue
    page.getBody
      .getOneHtmlElementByAttribute("div", "id", "flowpanes")
      .asInstanceOf[HtmlDivision]
      .getElementsByAttribute("div", "class", "fp-item")
      .asScala
      .asInstanceOf[mutable.Buffer[HtmlDivision]]
      .flatMap { item =>
      regionToCity.get(region).orElse(regionToCity.get(URLDecoder.decode(region, StandardCharsets.UTF_8.name()))).flatMap { city =>
        extractEntry(
          "Дикси",
          city,
          item.getOneHtmlElementByAttribute("div", "class", "prices-3")
            .asInstanceOf[HtmlDivision]
            .getTextContent
            .replace("весовая", "")
            .replace("весовой", ""),
          extractPrice(item.getElementsByAttribute("h5", "class", "price-now threedigit")
            .asScala
            .headOption
            .asInstanceOf[Option[HtmlHeading5]]
            .getOrElse(item.getOneHtmlElementByAttribute("h5", "class", "price-now").asInstanceOf[HtmlHeading5])
            .getTextContent),
          null,
          item.getElementsByTagName("img")
            .asScala
            .asInstanceOf[mutable.Buffer[HtmlDivision]]
            .headOption
            .orNull
        )
      }
    }.iterator
  }
}
