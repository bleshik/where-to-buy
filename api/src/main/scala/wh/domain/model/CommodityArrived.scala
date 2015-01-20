package wh.domain.model

import eventstore.api.InitialEvent

case class CommodityArrived(shop: String, name: String, price: Long) extends InitialEvent[Commodity] {
  override def initializedObject(): Commodity = Commodity.arrived(shop, name, price)
}
