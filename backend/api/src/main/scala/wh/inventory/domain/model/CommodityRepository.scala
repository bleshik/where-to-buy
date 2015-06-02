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
   * Returns average prices for all shops a commodity had.
   * @param commodityName name of a commodity.
   * @param city if None, it will use prices across the country.
   * @return history.
   */
  def averagePrices(commodityName: String, city: Option[String]): Option[PricesHistory]

  /**
   * Returns for a specific shop a commodity had.
   * @param commodityName name of a commodity.
   * @param shop a shop.
   * @return history.
   */
  def prices(commodityName: String, shop: Shop): Option[PricesHistory]
}
