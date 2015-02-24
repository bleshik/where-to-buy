package wh.application.extractor.dixy

import java.net.URL

import com.gargoylesoftware.htmlunit.html.{HtmlDivision, HtmlHeading5, HtmlPage}
import com.gargoylesoftware.htmlunit.util.Cookie
import wh.application.extractor.AbstractHtmlUnitExtractor
import wh.extractor.domain.model.ExtractedEntry

import scala.collection.JavaConverters._
import scala.collection.mutable

class DixyExtractor extends AbstractHtmlUnitExtractor {
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

  override def extract(url: URL): Iterator[ExtractedEntry] = regionToCity.iterator.flatMap { region =>
    client.getCookieManager.addCookie(new Cookie(url.getHost, dixyRegion, region._1))
    super.extract(url)
  }

  override def doExtract(page: HtmlPage): Iterator[ExtractedEntry] = {
    page.getBody
      .getOneHtmlElementByAttribute("div", "id", "flowpanes")
      .asInstanceOf[HtmlDivision]
      .getElementsByAttribute("div", "class", "fp-item")
      .asScala
      .asInstanceOf[mutable.Buffer[HtmlDivision]]
      .flatMap { item =>
      extractEntry(
        "Дикси",
        regionToCity.get(client.getCookieManager.getCookie(dixyRegion).getValue).get,
        item.getOneHtmlElementByAttribute("div", "class", "prices-3")
            .asInstanceOf[HtmlDivision]
            .getTextContent
            .replace("весовая", "")
            .replace("весовой", ""),
        (BigDecimal(
          item.getElementsByAttribute("h5", "class", "price-now threedigit")
            .asScala
            .headOption
            .asInstanceOf[Option[HtmlHeading5]]
            .getOrElse(item.getOneHtmlElementByAttribute("h5", "class", "price-now").asInstanceOf[HtmlHeading5])
            .getTextContent
            .replace(",", ".")) * 100
        ).toLong,
        null,
        item.getElementsByTagName("img")
          .asScala
          .asInstanceOf[mutable.Buffer[HtmlDivision]]
          .headOption
          .orNull
      )
    }.iterator
  }
}
