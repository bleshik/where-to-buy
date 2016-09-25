package wh.inventory.domain.model

import ddd.repository.eventsourcing.IdentifiedEventSourcedEntity

case class Commodity private(name: String, entries: List[Entry], tags: Set[String])
  extends IdentifiedEventSourcedEntity[Commodity, String](name, CommodityArrived(entries.head.shop, name, entries.head.price, tags)) {

  def this(event: CommodityArrived) = this(event.name, List(Entry(event.shop, event.name, event.price)), event.tags)

  def this(shop: Shop, name: String, price: Long, tags: Set[String] = Set.empty) =
    this(CommodityArrived(shop, name, price, tags))

  def id: String = name

  def arrived(shop: Shop, shopSpecificName:String, price: Long, tags: Set[String] = Set.empty): Commodity = {
    apply(CommodityArrived(shop, shopSpecificName, price, tags))
  }

  def changePrice(shop: Shop, price: Long): Commodity = {
    apply(CommodityPriceChanged(shop, name, price))
  }

  def tag(tag: String): Commodity = {
    apply(CommodityTagged(name, tag))
  }

  def entry(shop: Shop): Option[Entry] =
    entries.find(e => e.shop.equals(shop))

  def price(shop: Shop): Option[Long] =
    entry(shop).map(_.price)

  def averagePrice: Long = {
    entries.map(_.price).sum / entries.size
  }

  private def allExcept(shop: Shop): List[Entry] =
    entries.filterNot(e => e.shop.equals(shop))

  protected def when(priceChanged: CommodityPriceChanged): Commodity = {
    val e = entry(priceChanged.shop)
    if (e.isEmpty) {
      throw new IllegalStateException("There is no such product in the shop where price was changed")
    }
    if (e.get.price == priceChanged.price) {
      throw new IllegalArgumentException(s"The passed price is same ${priceChanged.price}")
    }
    copy(entries = allExcept(priceChanged.shop) :+ Entry(priceChanged.shop, e.get.shopSpecificName, priceChanged.price))
  }

  protected def when(arrived: CommodityArrived): Commodity = {
    if (entry(arrived.shop).isDefined) {
      throw new IllegalStateException("Product can arrive to the same shop just once")
    }
    copy(entries = entries :+ Entry(arrived.shop, arrived.name, arrived.price), tags = tags ++ arrived.tags)
  }

  protected def when(tagged: CommodityTagged): Commodity = {
    copy(tags = tags + tagged.tag)
  }
}
