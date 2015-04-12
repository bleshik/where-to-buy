package wh.application.extractor.komus

import java.net.URL

import org.scalatest.{FlatSpec, Matchers}
import wh.extractor.domain.model.{Category, ExtractedEntry, ExtractedShop}

class KomusExtractorSpec extends FlatSpec with Matchers {
  "Komus extractor" should "return right list of entries" in {
    val extractor = new KomusExtractor
    val category = Category("Бумага туалетная бытовая",Category("Туалетная бумага",Category("Бумажная продукция и держатели",null)))
    val page = getClass.getClassLoader.getResource("komus/1.html")
    val root = page.toURI.resolve("./").toString
    extractor.extract(page).toList should be (
      List(
        ExtractedEntry(ExtractedShop("Komus", "Москва"), "Бумага туалетная «Островская» (1-слойная, натуральный цвет)", 660, category, new URL("http://www.komus.ru/photo/_normal/168190_1.jpg")),
        ExtractedEntry(ExtractedShop("Komus", "Москва"), "Бумага туалетная 54 метра «Мягкий знак» (однослойная, белая с тиснением)", 942, category, new URL("http://www.komus.ru/photo/_normal/214165_1.jpg"))
      )
    )
  }
}
