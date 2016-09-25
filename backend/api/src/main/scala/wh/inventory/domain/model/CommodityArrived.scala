package wh.inventory.domain.model

case class CommodityArrived(shop: Shop, name: String, price: Long, tags: Set[String] = Set.empty)
