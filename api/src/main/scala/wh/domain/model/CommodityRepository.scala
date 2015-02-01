package wh.domain.model

import repository.PersistenceOrientedRepository

trait CommodityRepository extends PersistenceOrientedRepository[Commodity, String] {
  /**
   * Returns the most similar commodity.
   * @param commodity commodity to compare with.
   * @return a commodity.
   */
  def findSimilar(commodity: Commodity): Option[Commodity]
}
