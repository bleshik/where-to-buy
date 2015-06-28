package wh.application.extractor.dixy

import java.net.URL

import org.scalatest.{Matchers, FlatSpec}
import wh.extractor.domain.model.{ExtractedEntry, ExtractedShop}

class DixyExtractorSpec extends FlatSpec with Matchers {
  "Dixy extractor" should "return right list of entries" in {
    val extractor = new DixyExtractor(Set("Москва"))
    val page = getClass.getClassLoader.getResource("dixy/1.html")
    val shop = ExtractedShop("Дикси", "Москва")
    extractor.extract(page).toList.distinct should be(List(
        ExtractedEntry(shop, "АБРИКОСЫ 1 кг", 13990,null,new URL("http://dixy.ru/sites/default/files/imagecache/dixy_action_block/actions/apricots_all_320.jpg")),
        ExtractedEntry(shop, "ВИНОГРАД светлый 1 кг", 15990,null,new URL("http://dixy.ru/sites/default/files/imagecache/dixy_action_block/actions/grapes_320.jpg")),
        ExtractedEntry(shop, "ГРУША зеленая 1 кг", 8490,null,new URL("http://dixy.ru/sites/default/files/imagecache/dixy_action_block/actions/pear_green_320.jpg")),
        ExtractedEntry(shop, "МОЛОКО Ультрапастеризованное Первым Делом 2,5% 900 мл", 2990,null,new URL("http://dixy.ru/sites/default/files/imagecache/dixy_action_block/actions/milk_ultra_pervim_delom.jpg")),
        ExtractedEntry(shop, "МАСЛО Традиционное Крестьянские узоры 82,5% пергамент 400 г", 13990,null,new URL("http://dixy.ru/sites/default/files/imagecache/dixy_action_block/actions/maslo_tradic_krestyansk_uzor_82.jpg")),
        ExtractedEntry(shop, "ШОКОЛАД Milka молочный в ассортименте 100 г", 5590,null,new URL("http://dixy.ru/sites/default/files/imagecache/dixy_action_block/actions/320x480_milka.jpg")),
        ExtractedEntry(shop, "КОФЕ Jardin Colombia Medellin растворимый 95 г", 14990,null,new URL("http://dixy.ru/sites/default/files/imagecache/dixy_action_block/actions/320x480_jardin.jpg")),
        ExtractedEntry(shop, "СТИРАЛЬНЫЕ ПОРОШКИ Ariel автомат Горный родник, Color автомат 3 кг", 23990,null,new URL("http://dixy.ru/sites/default/files/imagecache/dixy_action_block/actions/320x480_ariel_bags.jpg")),
        ExtractedEntry(shop, "СРЕДСТВО ДЛЯ СТИРКИ Ariel Горный родник Гель, Жидкие капсулы 3ХК Color 1,1 л, 15 шт *28,8 г", 23990, null, new URL("http://dixy.ru/sites/default/files/imagecache/dixy_action_block/actions/320x480_ariel.jpg"))
      )
    )
  }
}
