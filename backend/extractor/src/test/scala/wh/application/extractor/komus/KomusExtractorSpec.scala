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
        ExtractedEntry(ExtractedShop("Komus", "Москва"),"Гуашь Disney Феи 6 цветов",8051,Category("Гуашь",null),new URL("file:/medias/sys_master/root/h1a/hf1/8904566964254.jpg")),
        ExtractedEntry(ExtractedShop("Komus", "Москва"),"Гуашь Луч желтая светлая",18100,Category("Гуашь",null),new URL("file:/medias/sys_master/root/hcf/h7a/8932587405342.jpg"))
      )
    )
  }
}
