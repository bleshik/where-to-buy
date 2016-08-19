package wh.application.extractor.auchan

import java.net.URL

import org.scalatest.{Matchers, FlatSpec}
import wh.extractor.domain.model.{ExtractedEntry, Category, ExtractedShop}

class AuchanExtractorSpec extends FlatSpec with Matchers {
  "Auchan extractor" should "return right list of entries" in {
    val extractor = new AuchanExtractor
    val page = getClass.getClassLoader.getResource("auchan/1.html")
    val category = Category("Пюре", Category("Питание", Category("Детские товары", null)))
    val shop = new ExtractedShop("Ашан", "Москва")
    extractor.extract(page).toSet should be (
      Set(
        ExtractedEntry(shop, "Пюре Сады Придонья яблочное, 125 г. с 4 мес.", 1200, category, new URL("http://www.auchan.ru/pokupki/media/catalog/product/cache/1/small_image/173x174/9df78eab33525d08d6e5fb8d27136e95/2/4/244978.jpg")),
        ExtractedEntry(shop, "Пюре Бабушкино Лукошко Треска-картофель, 100 г. с 8 мес.", 3200, category, new URL("http://www.auchan.ru/pokupki/media/catalog/product/cache/1/small_image/173x174/9df78eab33525d08d6e5fb8d27136e95/3/5/353237_18.jpg"))
      )
    )
  }
}
