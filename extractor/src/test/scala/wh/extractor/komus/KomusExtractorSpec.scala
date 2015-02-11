package wh.extractor.komus

import java.net.URL

import org.scalatest.{FlatSpec, Matchers}
import wh.extractor.{Category, ExtractedEntry}

class KomusExtractorSpec extends FlatSpec with Matchers {
  "Komus extractor" should "return right list of entries" in {
    val extractor = new KomusExtractor
    val category = Category("Бумага туалетная бытовая",Category("Туалетная бумага",Category("Бумажная продукция и держатели",null)))
    val page = getClass.getClassLoader.getResource("komus/1.html")
    val root = page.toURI.resolve("./").toString
    extractor.extract(page).toList should be (
      List(
        ExtractedEntry("Komus", "Бумага туалетная «Островская» (1-слойная, натуральный цвет)", 660, category, new URL("http://www.komus.ru/photo/_normal/168190_1.jpg")),
        ExtractedEntry("Komus", "Бумага туалетная Linia Veiro Classic (2-слойная, белая, 24 рулона в упаковке)", 25771,category, new URL("http://www.komus.ru/photo/_normal/326384_1.jpg")),
        ExtractedEntry("Komus", "Бумага туалетная Zewa Plus (2-слойная, белая, 12 рулонов в упаковке)", 19080,category, new URL("http://www.komus.ru/photo/_normal/200600_1.jpg")),
        ExtractedEntry("Komus", "Бумага туалетная Zewa Plus (2-слойная, белая, 8 рулонов в упаковке)", 13580,category, new URL("http://www.komus.ru/photo/_normal/354659_1.jpg")),
        ExtractedEntry("Komus", "Бумага туалетная Mola (2-слойная, белая, 8 рулонов в упаковке)", 12830,category, new URL("http://www.komus.ru/photo/_normal/76481_1.jpg")),
        ExtractedEntry("Komus", "Бумага туалетная Linia Veiro Classic (2-слойная, белая, 12 рулонов в упаковке)", 12680,category, new URL("http://www.komus.ru/photo/_normal/393869_1.jpg")),
        ExtractedEntry("Komus", "Бумага туалетная Lambi (3-слойная, белая, 12 рулонов в упаковке)", 28332,category, new URL("http://www.komus.ru/photo/_normal/327118_1.jpg")),
        ExtractedEntry("Komus", "Бумага туалетная Zewa Plus (2-слойная, желтая, 12 рулонов в упаковке)", 19080,category, new URL("http://www.komus.ru/photo/_normal/200599_1.jpg")),
        ExtractedEntry("Komus", "Бумага туалетная Linia Veiro Classic (2-слойная, белая, 4 рулона в упаковке)", 4521,category, new URL("http://www.komus.ru/photo/_normal/221984_1.jpg")),
        ExtractedEntry("Komus", "Бумага туалетная «Сыктывкарская» (1-слойная, белая)", 1018,category, new URL("http://www.komus.ru/photo/_normal/10521_1.jpg")),
        ExtractedEntry("Komus", "Бумага туалетная Veiro (2-слойная, белая, 8 рулонов в упаковке)", 7447,category, new URL("http://www.komus.ru/photo/_normal/175133_1.jpg")),
        ExtractedEntry("Komus", "Бумага туалетная Zewa Plus (2-слойная, белая, 4 рулона в упаковке)", 6122,category, new URL("http://www.komus.ru/photo/_normal/21667_1.jpg")),
        ExtractedEntry("Komus", "Бумага туалетная Zewa Plus (2-слойная, зеленая, 8 рулонов в упаковке)", 13580,category, new URL("http://www.komus.ru/photo/_normal/200598_1.jpg")),
        ExtractedEntry("Komus", "Бумага туалетная Lambi (3-слойная, белая, 8 рулонов в упаковке)", 21527,category, new URL("http://www.komus.ru/photo/_normal/116929_1.jpg")),
        ExtractedEntry("Komus", "Бумага туалетная «Мягкий знак» (2-слойная, белая, 4 рулона в упаковке)", 4191,category, new URL("http://www.komus.ru/photo/_normal/214166_1.jpg")),
        ExtractedEntry("Komus", "Бумага туалетная 54 метра «Мягкий знак» (однослойная, белая с тиснением)", 942,category, new URL("http://www.komus.ru/photo/_normal/214165_1.jpg")),
        ExtractedEntry("Komus", "Бумага туалетная Luscan Professional (2-слойная, белая, 24 рулона в упаковке)", 30284,category, new URL("http://www.komus.ru/photo/_normal/396249_1.jpg")),
        ExtractedEntry("Komus", "Бумага туалетная Luscan Standart (2-слойная, белая, 4 рулона в упаковке)", 5115,category, new URL("http://www.komus.ru/photo/_normal/317381_1.jpg")),
        ExtractedEntry("Komus", "Бумага туалетная Zewa (2-слойная, зеленая, 12 рулонов в упаковке)", 19080,category, new URL("http://www.komus.ru/photo/_normal/322916_1.jpg")),
        ExtractedEntry("Komus", "Бумага туалетная Mola (2-слойная, розовая, 8 рулонов в упаковке)", 12830,category, new URL("http://www.komus.ru/photo/_normal/106361_1.jpg")),
        ExtractedEntry("Komus", "Бумага туалетная Luscan Comfort (2-слойная, желтая, 4 рулона в упаковке)", 4915,category, new URL("http://www.komus.ru/photo/_normal/317387_1.jpg")),
        ExtractedEntry("Komus", "Бумага туалетная Veiro (2-слойная, розовая, 4 рулона в упаковке)", 5015,category, new URL("http://www.komus.ru/photo/_normal/118632_1.jpg")),
        ExtractedEntry("Komus", "Бумага туалетная Zewa Plus (2-слойная, белая, 4 рулона в упаковке)", 7162,category, new URL("http://www.komus.ru/photo/_normal/354658_1.jpg")),
        ExtractedEntry("Komus", "Бумага туалетная «Мягкий знак» Flowers (2-слойная, желтая, 8 рулонов в упаковке)", 10049,category, new URL("http://www.komus.ru/photo/_normal/221993_1.jpg")),
        ExtractedEntry("Komus", "Бумага туалетная 54 метра «Рулончик» (однослойная, натуральный цвет)", 730,category, new URL("http://www.komus.ru/photo/_normal/393821_1.jpg")),
        ExtractedEntry("Komus", "Бумага туалетная Lambi (3-слойная, белая, 8 рулонов в упаковке)", 21553,category, new URL("http://www.komus.ru/photo/_normal/332584_1.jpg")),
        ExtractedEntry("Komus", "Бумага туалетная Mola (2-слойная, персик, 8 рулонов в упаковке)", 12830,category, new URL("http://www.komus.ru/photo/_normal/76482_1.jpg")),
        ExtractedEntry("Komus", "Бумага туалетная Luscan Comfort (2-слойная, белая, 4 рулона в упаковке)", 4900,category, new URL("http://www.komus.ru/photo/_normal/317384_1.jpg")),
        ExtractedEntry("Komus", "Бумага туалетная Mola (2-слойная, зеленая, 8 рулонов в упаковке)", 12830,category, new URL("http://www.komus.ru/photo/_normal/106362_1.jpg")),
        ExtractedEntry("Komus", "Бумага туалетная Luscan Comfort (2-слойная, белая, 8 рулонов в упаковке)", 12016,category, new URL("http://www.komus.ru/photo/_normal/396250_1.jpg")),
        ExtractedEntry("Komus", "Бумага туалетная Zewa Deluxe (3-слойная, персик, 16 рулонов в упаковке)", 43529,category, new URL("http://www.komus.ru/photo/_normal/322919_1.jpg")),
        ExtractedEntry("Komus", "Бумага туалетная Zewa Plus (2-слойная, зеленая, 4 рулона в упаковке)", 7162,category, new URL("http://www.komus.ru/photo/_normal/55993_1.jpg")),
        ExtractedEntry("Komus", "Бумага туалетная Luscan Comfort (2-слойная, зеленая, 4 рулона в упаковке)", 4915,category, new URL("http://www.komus.ru/photo/_normal/317390_1.jpg")),
        ExtractedEntry("Komus", "Бумага туалетная «Рулончик большой» (1-слойная, натуральный цвет)", 991,category, new URL("http://www.komus.ru/photo/_normal/393822_1.jpg")),
        ExtractedEntry("Komus", "Бумага туалетная Veiro (2-слойная, голубая, 4 рулона в упаковке)", 5319,category, new URL("http://www.komus.ru/photo/_normal/118631_1.jpg")),
        ExtractedEntry("Komus", "Бумага туалетная Luscan Standart (2-слойная, белая, 8 рулонов в упаковке)", 10439,category, new URL("http://www.komus.ru/photo/_normal/396251_1.jpg")),
        ExtractedEntry("Komus", "Бумага туалетная Zewa Plus (2-слойная, желтая, 4 рулона в упаковке)", 6980,category, new URL("http://www.komus.ru/photo/_normal/55994_1.jpg")),
        ExtractedEntry("Komus", "Бумага туалетная Lambi (4-слойная, белая, 6 рулонов в упаковке)", 18857,category, new URL("http://www.komus.ru/photo/_normal/327117_1.jpg")),
        ExtractedEntry("Komus", "Бумага туалетная «Мягкий знак» Premium (3-слойная, белая, 4 рулона в упаковке)", 7492,category, new URL("http://www.komus.ru/photo/_normal/214167_1.jpg")),
        ExtractedEntry("Komus", "Бумага туалетная «Из Набережных Челнов» (1-слойная, натуральный цвет)", 1126,category, new URL("http://www.komus.ru/photo/_normal/399511_1.jpg")),
        ExtractedEntry("Komus", "Бумага туалетная Veiro (2-слойная, желтая, 4 рулона в упаковке)", 5015,category, new URL("http://www.komus.ru/photo/_normal/118633_1.jpg")),
        ExtractedEntry("Komus", "Бумага туалетная Zewa Deluxe (3-слойная, белая, 4 рулона в упаковке)", 11370,category, new URL("http://www.komus.ru/photo/_normal/221988_1.jpg")),
        ExtractedEntry("Komus", "Бумага туалетная Mola (2-слойная, белая, 4 рулона в упаковке)", 6422,category, new URL("http://www.komus.ru/photo/_normal/132646_1.jpg")),
        ExtractedEntry("Komus", "Бумага туалетная Zewa Exclusive (4-слойная, белая, 6 рулонов в упаковке)", 30383,category, new URL("http://www.komus.ru/photo/_normal/340301_1.jpg")),
        ExtractedEntry("Komus", "Бумага туалетная Lambi (3-слойная, белая, 4 рулона в упаковке)", 10907,category, new URL("http://www.komus.ru/photo/_normal/211862_1.jpg"))

      )
    )
  }
}
