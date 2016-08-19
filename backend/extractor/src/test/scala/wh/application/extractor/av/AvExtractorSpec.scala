package wh.application.extractor.av

import java.net.URL

import org.scalatest.{FlatSpec, Matchers}
import wh.application.extractor.SupportedCity
import wh.extractor.domain.model.{Category, ExtractedShop, ExtractedEntry}

class AvExtractorSpec extends FlatSpec with Matchers {
  val extractor = new AvExtractor
  val shop = new ExtractedShop("Азбука Вкуса", SupportedCity.Moscow.name)

  "Av extractor" should "return right list of entries for flat category list" in {
    val page = getClass.getClassLoader.getResource("av/flat/1.html")
    val category = Category("Автокосметика и автоаксессуары")
    extractor.extract(page).toSet should be (
      Set(
        ExtractedEntry(
          shop, "Жидкость незамерзающая Просто Азбука - 20' 5л Россия", 39700, category, new URL("file:/images/cms/data/items/272635/table/272635.jpg")
        ),
        ExtractedEntry(
          shop, "Салфетка Сапфир Large&Soft микрофибра 1 шт.", 19200, category, new URL("file:/images/cms/data/items/219971/table/219971.jpg")
        )
      )
    )
  }

  "Av extractor" should "return right list of entries for category list with sub-menus" in {
    val page = getClass.getClassLoader.getResource("av/submenu/1.html")
    val category = Category("Продукты быстрого приготовления",Category("Бакалея"))
    extractor.extract(page).toSet should be (
      Set(
        ExtractedEntry(
          shop, "Жидкость незамерзающая Просто Азбука - 20' 5л Россия", 39700, category, new URL("file:/images/cms/data/items/272635/table/272635.jpg")
        ),
        ExtractedEntry(
          shop, "Салфетка Сапфир Large&Soft микрофибра 1 шт.", 19200, category, new URL("file:/images/cms/data/items/219971/table/219971.jpg")
        )
      )
    )
  }
}
