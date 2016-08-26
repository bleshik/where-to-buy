package wh.application.extractor.dixy

import java.net.URL

import org.scalatest.{Matchers, FlatSpec}
import wh.extractor.domain.model.{Category, ExtractedEntry, ExtractedShop}

class DixyExtractorSpec extends FlatSpec with Matchers {
  "Dixy extractor" should "return right list of entries" in {
    val extractor = new DixyExtractor
    val page = getClass.getClassLoader.getResource("dixy/1.html")
    val shop = new ExtractedShop("Дикси", "Москва")
    extractor.extract(page).filter { entry => entry.shop.region.name.equals(shop.city.get) }.toList.distinct should be(List(
      ExtractedEntry(shop, "Нектарины 1 кг", 9900, Category("Овощи и фрукты", null), new URL("file:/upload/iblock/833/DI00081186.jpg"))
    ))
  }
}
