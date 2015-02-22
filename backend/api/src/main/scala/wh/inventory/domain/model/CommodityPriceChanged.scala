package wh.inventory.domain.model

import eventstore.api.Event

case class CommodityPriceChanged(shop: Shop, name: String, price: Long) extends Event
