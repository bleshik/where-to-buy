package wh.application.extractor.utkonos

import java.net.URL

import org.scalatest.{FlatSpec, Matchers}
import wh.extractor.domain.model.{ExtractedEntry, ExtractedShop, Category}

class UtkonosExtractorSpec extends FlatSpec with Matchers {
  val rootCategory = Category("Продукты питания",null)
  val firstCategory = Category("Молочные продукты, мороженое", rootCategory)
  val secondCategory = Category("Молоко, сливки, молочные коктейли", firstCategory)
  "Utkonos extractor" should "return right list of entries" in {
    val extractor = new UtkonosExtractor
    val page = getClass.getClassLoader.getResource("utkonos/1.html")
    val root = page.toURI
    val shop = new ExtractedShop("Утконос", "Москва")
    extractor.extract(page).toList should be (
      List(
        ExtractedEntry(shop, "Молоко М Лианозовское ультрапастеризованное 3,2%, 950г", 5180, secondCategory, new URL("file:/images/photo/3074/3074902B.jpg?1337770127")),
        ExtractedEntry(shop, "Сливки Домик в деревне 10% стерилизованные, 480г", 7470, secondCategory, new URL("file:/images/photo/3051/3051861B.jpg?1342618109")),
        ExtractedEntry(shop, "Сливки Домик в деревне 20%, 200г", 7180, secondCategory, new URL("file:/images/photo/3049/3049025B.jpg?1337876707"))
      )
    )
  }
}
