package wh.inventory.domain.model

import eventstore.api.InitialEvent

case class CommodityArrived(shop: String, name: String, price: Long, tags: Set[String] = Set.empty) extends InitialEvent[Commodity] {
  override def initializedObject(): Commodity = Commodity.arrived(shop, name, price, tags)
}
