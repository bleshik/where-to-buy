package wh.domain.model

import repository.eventsourcing.IdentifiedEventSourcedEntity

case class Commodity private(name: String, entries: List[Entry], tags: List[String])
  extends IdentifiedEventSourcedEntity[Commodity, String](CommodityArrived(entries.head.shop, name, entries.head.price)) {

  def id: String = name

  def arrived(shop: String, shopSpecificName:String, price: Long): Commodity = {
    apply(CommodityArrived(shop, shopSpecificName, price))
  }

  def changePrice(shop: String, price: Long): Commodity = {
    apply(CommodityPriceChanged(shop, name, price))
  }

  def entryInShop(shop: String): Option[Entry] =
    entries.find(e => e.shop.equals(shop))

  private def allExcept(shop: String): List[Entry] =
    entries.filterNot(e => e.shop.equals(shop))

  protected def when(priceChanged: CommodityPriceChanged): Commodity = {
    if (entryInShop(priceChanged.shop).isEmpty) {
      throw new IllegalStateException("There is no such product in the shop where price was changed")
    }
    copy(entries = allExcept(priceChanged.shop) :+ Entry(priceChanged.shop, priceChanged.price))
  }

  protected def when(arrived: CommodityArrived): Commodity = {
    if (entryInShop(arrived.shop).isDefined) {
      throw new IllegalStateException("Product can arrive to the same shop just once")
    }
    copy(entries = entries :+ Entry(arrived.shop, arrived.price))
  }
}

object Commodity {
  def arrived(shop: String, name: String, price: Long): Commodity = {
    new Commodity(name, List(Entry(shop, price)), List())
  }
}
