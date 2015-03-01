package wh.application.extractor.metro

import java.net.URL

import org.scalatest.{FlatSpec, Matchers}
import wh.extractor.domain.model.{Category, ExtractedShop, ExtractedEntry}

class MetroExtractorSpec extends FlatSpec with Matchers {
  "Metro extractor" should "return right list of entries" in {
    val url = getClass.getClassLoader.getResource("metro/1.html")
    val extractor = new MetroExtractor {
      override protected def domains(u: URL): Map[String, URL] = Map("Москва" -> u)
      override protected def entriesUrls(categoryUrl: URL): List[URL] = List(new URL(categoryUrl.toString.replace(".html", ".json")))
    }
    extractor.extract(url).toSet should be (
      Set(
        ExtractedEntry(
          ExtractedShop("Metro", "Москва"),
          "Вино Bordeaux Reserve Des Princes красное сухое, 12,5% 0,75л",
          40001,
          Category("Вино", Category("Алкогольная продукция", Category("Продукты", null))),
          new URL("http://static.metro-cc.ru/data/public/image/cache/441262-1-150x150.jpg")
        )
      )
    )
  }
}
