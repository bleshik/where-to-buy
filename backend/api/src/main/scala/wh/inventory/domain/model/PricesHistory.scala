package wh.inventory.domain.model

/**
 * @param commodityName name of a commodity.
 * @param history list of (timestamp, price) pairs ordered by timestamp.
 */
case class PricesHistory(commodityName: String, history: List[(Long, Long)])
