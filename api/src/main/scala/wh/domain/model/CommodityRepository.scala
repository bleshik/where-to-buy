package wh.domain.model

import repository.PersistenceOrientedRepository

trait CommodityRepository extends PersistenceOrientedRepository[Commodity, String]
