package wh.inventory.domain.model

import repository.PersistenceOrientedRepository

trait CommodityRepository extends PersistenceOrientedRepository[Commodity, String] {
  /**
   * Returns the most similar commodity.
   * @param commodity commodity to compare with.
   * @return a commodity.
   */
  def findSimilar(commodity: Commodity): Option[Commodity]

  /**
   * Finds list of commodities using the search pattern.
   * @param searchPattern a pattern used for search.
   * @return list of commodities.
   */
  def search(searchPattern: String): List[Commodity]
}