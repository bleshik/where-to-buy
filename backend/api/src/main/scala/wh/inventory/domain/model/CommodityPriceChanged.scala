package wh.inventory.domain.model

import eventstore.api.Event

case class CommodityPriceChanged(shop: String, name: String, price: Long) extends Event
