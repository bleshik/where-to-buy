package wh.application.commodity

import javax.inject.Inject

import wh.inventory.domain.model.CommodityRepository

import scala.util.Random

class CommodityService @Inject()(commodityRepository: CommodityRepository) {
  def randomInterestingCommodity(city: String) = {
    Random.shuffle(commodityRepository.search("", city, 100, 0)).head
  }
}
