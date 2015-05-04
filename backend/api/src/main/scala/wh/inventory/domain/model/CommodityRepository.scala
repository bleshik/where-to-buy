package wh.inventory.domain.model

import repository.eventsourcing.TemporalPersistenceOrientedRepository

trait CommodityRepository extends TemporalPersistenceOrientedRepository[Commodity, String] {
  /**
   * Returns the most similar commodity.
   * @param commodity commodity to compare with.
   * @return a commodity.
   */
  def findSimilar(commodity: Commodity): Option[Commodity]

  /**
   * Finds list of commodities using the search pattern.
   * @param searchPattern a pattern used for search.
   * @param city a city where to look for commodities.
   * @return list of commodities.
   */
  def search(searchPattern: String, city: String, limit: Int, offset: Int): List[Commodity]

  /**
   * Returns all prices a commodity had.
   * @param commodityName name of a commodity.
   * @param shop a shop.
   * @return list of (timestamp, price) pairs ordered by timestamp.
   */
  def pricesHistory(commodityName: String, shop: Shop): List[(Long, Long)]
}
