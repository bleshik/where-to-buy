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
        ExtractedEntry(shop, "СЛИВА 1 кг", 15990, null, new URL("http://dixy.ru/sites/default/files/imagecache/dixy_action_block/actions/plum_2_mru_24.02.2015.jpg")),
        ExtractedEntry(shop, "ГРУША зеленая 1 кг", 10990, null, new URL("http://dixy.ru/sites/default/files/imagecache/dixy_action_block/actions/pear_green_mru_24.02.2015.jpg")),
        ExtractedEntry(shop, "КАРТОФЕЛЬ молодой 1 кг", 4990, null, new URL("http://dixy.ru/sites/default/files/imagecache/dixy_action_block/actions/potatoes_mru_24.02.2015.jpg")),
        ExtractedEntry(shop, "МАКАРОННЫЕ ИЗДЕЛИЯ Макфа петушиные гребешки, перья любительские, улитки 450 г", 3290, null, new URL("http://dixy.ru/sites/default/files/imagecache/dixy_action_block/actions/makfa_3_mru_24.02.2015.jpg")),
        ExtractedEntry(shop, "ПАШТЕТ Нежный из птичьей печени ЕКГФ 150 г", 5690, null, new URL("http://dixy.ru/sites/default/files/imagecache/dixy_action_block/actions/pashtet_pate_soft_nejniy_bird_liver_ekgf_150g_mru_24.02.2015.jpg")),
        ExtractedEntry(shop, "КОЛБАСА Балыковая Велком 400 г", 27990, null, new URL("http://dixy.ru/sites/default/files/imagecache/dixy_action_block/actions/kolbasa_balik_velkom_400g_2pcs_mru_24.02.2015.jpg")),
        ExtractedEntry(shop, "ПРОДУКТ МОЛОЧНЫЙ в вафельной трубочке Варенка 12% 70 г", 1690, null, new URL("http://dixy.ru/sites/default/files/imagecache/dixy_action_block/actions/trubochki_cru_24.02.2015.jpg")),
        ExtractedEntry(shop, "ЙОГУРТ Активиа 2,9% - 3% в ассортименте 150 г", 1990, null, new URL("http://dixy.ru/sites/default/files/imagecache/dixy_action_block/actions/activia_mru_24.02.2015.jpg")),
        ExtractedEntry(shop, "КОФЕ Milagro Gold Roast растворимый 75 г", 12990, null, new URL("http://dixy.ru/sites/default/files/imagecache/dixy_action_block/actions/coffe_milagro_gold_roast_75g_mru_24.02.2015.jpg")),
        ExtractedEntry(shop, "ЧАЙ Ahmad Tea зеленый листовой 90 г/100 г", 5990, null, new URL("http://dixy.ru/sites/default/files/imagecache/dixy_action_block/actions/tea_ahmad_green_list_100g_mru_24.02.2015.jpg")),
        ExtractedEntry(shop, "СРЕДСТВО ДЛЯ СТИРКИ Tide Весенние цветы гель 1,3 л", 21990, null, new URL("http://dixy.ru/sites/default/files/imagecache/dixy_action_block/actions/tide_gel_alpine_freshness_13l_mru_24.02.2015.jpg")),
        ExtractedEntry(shop, "СРЕДСТВО ДЛЯ СТИРКИ Миф Морозная свежесть гель 1,6 л", 21990, null, new URL("http://dixy.ru/sites/default/files/imagecache/dixy_action_block/actions/mif_gel_frosty_freshness_1.6l_mru_24.02.2015.jpg"))
      ))
  }
}
