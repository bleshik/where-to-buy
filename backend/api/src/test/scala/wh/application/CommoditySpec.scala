package wh.application

import org.scalatest.{Matchers, FlatSpec}
import wh.inventory.domain.model.{Shop, Entry, Commodity}

class CommoditySpec extends FlatSpec with Matchers {
  "Commodity" should "be arrived to same shop only once" in {
    val commodity = Commodity.arrived(Shop.Utkonos, "Toilet Paper", 10000)
      .arrived(Shop.Komus, "Toilet Paper", 11000)
    commodity.entry(Shop.Utkonos) should be(Some(Entry(Shop.Utkonos, "Toilet Paper", 10000)))
    commodity.entry(Shop.Komus) should be(Some(Entry(Shop.Komus, "Toilet Paper", 11000)))
    a [IllegalStateException] shouldBe thrownBy {
      commodity.arrived(Shop.Utkonos, "Oops", 10000)
    }
  }

  "Commodity" should "can change price only in shops were it is being sold" in {
    val commodity = Commodity.arrived(Shop.Utkonos, "Toilet Paper", 10000)
      .changePrice(Shop.Utkonos, 12000)
    commodity.entry(Shop.Utkonos) should be(Some(Entry(Shop.Utkonos, "Toilet Paper", 12000)))
    a [IllegalStateException] shouldBe thrownBy {
      commodity.changePrice(Shop.Komus, 12000)
    }
  }
}
