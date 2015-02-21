package wh.inventory.domain.model

import eventstore.api.InitialEvent

case class CommodityArrived(shop: Shop, name: String, price: Long, tags: Set[String] = Set.empty) extends InitialEvent[Commodity] {
  override def initializedObject(): Commodity = Commodity.arrived(shop, name, price, tags)
}
