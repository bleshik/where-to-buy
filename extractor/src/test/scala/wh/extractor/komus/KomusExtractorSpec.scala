package wh.extractor.komus

import org.scalatest.{FlatSpec, Matchers}
import wh.extractor.{Category, ExtractedEntry}

class KomusExtractorSpec extends FlatSpec with Matchers {
  "Komus extractor" should "return right list of entries" in {
    val extractor = new KomusExtractor(downloadImages = false)
    val category = Category("Бумага туалетная бытовая",Category("Туалетная бумага",Category("Бумажная продукция и держатели",null)))
    extractor.extract(getClass.getClassLoader.getResource("komus/1.html")).toList should be (
      List(
        ExtractedEntry("Komus", "Бумага туалетная «Островская» (1-слойная, натуральный цвет)", 660, category),
        ExtractedEntry("Komus", "Бумага туалетная Linia Veiro Classic (2-слойная, белая, 24 рулона в упаковке)", 25771,category),
        ExtractedEntry("Komus", "Бумага туалетная Zewa Plus (2-слойная, белая, 12 рулонов в упаковке)", 19080,category),
        ExtractedEntry("Komus", "Бумага туалетная Zewa Plus (2-слойная, белая, 8 рулонов в упаковке)", 13580,category),
        ExtractedEntry("Komus", "Бумага туалетная Mola (2-слойная, белая, 8 рулонов в упаковке)", 12830,category),
        ExtractedEntry("Komus", "Бумага туалетная Linia Veiro Classic (2-слойная, белая, 12 рулонов в упаковке)", 12680,category),
        ExtractedEntry("Komus", "Бумага туалетная Lambi (3-слойная, белая, 12 рулонов в упаковке)", 28332,category),
        ExtractedEntry("Komus", "Бумага туалетная Zewa Plus (2-слойная, желтая, 12 рулонов в упаковке)", 19080,category),
        ExtractedEntry("Komus", "Бумага туалетная Linia Veiro Classic (2-слойная, белая, 4 рулона в упаковке)", 4521,category),
        ExtractedEntry("Komus", "Бумага туалетная «Сыктывкарская» (1-слойная, белая)", 1018,category),
        ExtractedEntry("Komus", "Бумага туалетная Veiro (2-слойная, белая, 8 рулонов в упаковке)", 7447,category),
        ExtractedEntry("Komus", "Бумага туалетная Zewa Plus (2-слойная, белая, 4 рулона в упаковке)", 6122,category),
        ExtractedEntry("Komus", "Бумага туалетная Zewa Plus (2-слойная, зеленая, 8 рулонов в упаковке)", 13580,category),
        ExtractedEntry("Komus", "Бумага туалетная Lambi (3-слойная, белая, 8 рулонов в упаковке)", 21527,category),
        ExtractedEntry("Komus", "Бумага туалетная «Мягкий знак» (2-слойная, белая, 4 рулона в упаковке)", 4191,category),
        ExtractedEntry("Komus", "Бумага туалетная 54 метра «Мягкий знак» (однослойная, белая с тиснением)", 942,category),
        ExtractedEntry("Komus", "Бумага туалетная Luscan Professional (2-слойная, белая, 24 рулона в упаковке)", 30284,category),
        ExtractedEntry("Komus", "Бумага туалетная Luscan Standart (2-слойная, белая, 4 рулона в упаковке)", 5115,category),
        ExtractedEntry("Komus", "Бумага туалетная Zewa (2-слойная, зеленая, 12 рулонов в упаковке)", 19080,category),
        ExtractedEntry("Komus", "Бумага туалетная Mola (2-слойная, розовая, 8 рулонов в упаковке)", 12830,category),
        ExtractedEntry("Komus", "Бумага туалетная Luscan Comfort (2-слойная, желтая, 4 рулона в упаковке)", 4915,category),
        ExtractedEntry("Komus", "Бумага туалетная Veiro (2-слойная, розовая, 4 рулона в упаковке)", 5015,category),
        ExtractedEntry("Komus", "Бумага туалетная Zewa Plus (2-слойная, белая, 4 рулона в упаковке)", 7162,category),
        ExtractedEntry("Komus", "Бумага туалетная «Мягкий знак» Flowers (2-слойная, желтая, 8 рулонов в упаковке)", 10049,category),
        ExtractedEntry("Komus", "Бумага туалетная 54 метра «Рулончик» (однослойная, натуральный цвет)", 730,category),
        ExtractedEntry("Komus", "Бумага туалетная Lambi (3-слойная, белая, 8 рулонов в упаковке)", 21553,category),
        ExtractedEntry("Komus", "Бумага туалетная Mola (2-слойная, персик, 8 рулонов в упаковке)", 12830,category),
        ExtractedEntry("Komus", "Бумага туалетная Luscan Comfort (2-слойная, белая, 4 рулона в упаковке)", 4900,category),
        ExtractedEntry("Komus", "Бумага туалетная Mola (2-слойная, зеленая, 8 рулонов в упаковке)", 12830,category),
        ExtractedEntry("Komus", "Бумага туалетная Luscan Comfort (2-слойная, белая, 8 рулонов в упаковке)", 12016,category),
        ExtractedEntry("Komus", "Бумага туалетная Zewa Deluxe (3-слойная, персик, 16 рулонов в упаковке)", 43529,category),
        ExtractedEntry("Komus", "Бумага туалетная Zewa Plus (2-слойная, зеленая, 4 рулона в упаковке)", 7162,category),
        ExtractedEntry("Komus", "Бумага туалетная Luscan Comfort (2-слойная, зеленая, 4 рулона в упаковке)", 4915,category),
        ExtractedEntry("Komus", "Бумага туалетная «Рулончик большой» (1-слойная, натуральный цвет)", 991,category),
        ExtractedEntry("Komus", "Бумага туалетная Veiro (2-слойная, голубая, 4 рулона в упаковке)", 5319,category),
        ExtractedEntry("Komus", "Бумага туалетная Luscan Standart (2-слойная, белая, 8 рулонов в упаковке)", 10439,category),
        ExtractedEntry("Komus", "Бумага туалетная Zewa Plus (2-слойная, желтая, 4 рулона в упаковке)", 6980,category),
        ExtractedEntry("Komus", "Бумага туалетная Lambi (4-слойная, белая, 6 рулонов в упаковке)", 18857,category),
        ExtractedEntry("Komus", "Бумага туалетная «Мягкий знак» Premium (3-слойная, белая, 4 рулона в упаковке)", 7492,category),
        ExtractedEntry("Komus", "Бумага туалетная «Из Набережных Челнов» (1-слойная, натуральный цвет)", 1126,category),
        ExtractedEntry("Komus", "Бумага туалетная Veiro (2-слойная, желтая, 4 рулона в упаковке)", 5015,category),
        ExtractedEntry("Komus", "Бумага туалетная Zewa Deluxe (3-слойная, белая, 4 рулона в упаковке)", 11370,category),
        ExtractedEntry("Komus", "Бумага туалетная Mola (2-слойная, белая, 4 рулона в упаковке)", 6422,category),
        ExtractedEntry("Komus", "Бумага туалетная Zewa Exclusive (4-слойная, белая, 6 рулонов в упаковке)", 30383,category),
        ExtractedEntry("Komus", "Бумага туалетная Lambi (3-слойная, белая, 4 рулона в упаковке)", 10907,category)
      )
    )
  }
}
