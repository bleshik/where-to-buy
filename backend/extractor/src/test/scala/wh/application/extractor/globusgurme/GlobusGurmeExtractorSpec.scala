package wh.application.extractor.globusgurme

import java.net.URL

import org.scalatest.{FlatSpec, Matchers}
import wh.application.extractor.globusgurme.GlobusGurmeExtractor
import wh.extractor.domain.model.{Category, ExtractedShop, ExtractedEntry}

class GlobusGurmeExtractorSpec extends FlatSpec with Matchers {
  "Globus Gurme extractor" should "return right list of entries" in {
    val extractor = new GlobusGurmeExtractor
    val shop = ExtractedShop("Глобус Гурмэ", "Москва")
    val category = Category("Йогурты", Category("Молочные продукты, яйцо", Category("Продовольственные товары",null)))
    val page = getClass.getClassLoader.getResource("globusgurme/1.html")
    extractor.extract(page).toSet should be (
      Set(
        ExtractedEntry(shop, "Биойогурт Данон Активиа питьевой ананас 290г Россия", 4800, category, new URL("file:/upload/iblock/92c/92cc3c5017de43562d1e7644bc73dd39158_158_thumb.png")),
        ExtractedEntry(shop, "Айва Узбекистан 1 шт", 15645, category, new URL("file:/upload/iblock/45b/45b262440e089d1517287acf5ba78de5158_158_thumb.png")),
        ExtractedEntry(shop, "Айва Узбекистан 1 кг", 44700, category, new URL("file:/upload/iblock/45b/45b262440e089d1517287acf5ba78de5158_158_thumb.png")),
        ExtractedEntry(shop, "Йогурт На лугу пасутся ко... сливочн домашн Черника 350 мл 6% Россия", 31900, category, new URL("file:/upload/iblock/926/9266e987798173207cf433af4fd8abf0158_158_thumb.png"))
      )
    )
  }
}
