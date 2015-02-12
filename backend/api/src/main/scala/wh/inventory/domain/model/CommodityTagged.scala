package wh.inventory.domain.model

import eventstore.api.Event

case class CommodityTagged(name: String, tag: String) extends Event
