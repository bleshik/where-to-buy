package wh.application

import org.scalatest.{Matchers, FlatSpec}
import wh.domain.model.{Entry, Commodity}

class CommoditySpec extends FlatSpec with Matchers {
  "Commodity" should "be arrived to same shop only once" in {
    val commodity = Commodity.arrived("Utkonos", "Toilet Paper", 10000)
      .arrived("Komus", "Toilet Paper", 11000)
    commodity.entryInShop("Utkonos") should be(Some(Entry("Utkonos", 10000)))
    commodity.entryInShop("Komus") should be(Some(Entry("Komus", 11000)))
    a [IllegalStateException] shouldBe thrownBy {
      commodity.arrived("Utkonos", "Oops", 10000)
    }
  }

  "Commodity" should "can change price only in shops were it is being sold" in {
    val commodity = Commodity.arrived("Utkonos", "Toilet Paper", 10000)
      .changePrice("Utkonos", 12000)
    commodity.entryInShop("Utkonos") should be(Some(Entry("Utkonos", 12000)))
    a [IllegalStateException] shouldBe thrownBy {
      commodity.changePrice("Komus", 12000)
    }
  }
}
