package wh.port.adapter.persistence

import com.mongodb.DB
import repository.eventsourcing.mongodb.MongoDbEventSourcedRepository
import wh.domain.model.{CommodityRepository, Commodity}

class MongoDbCommodityRepository(override val db: DB)
  extends MongoDbEventSourcedRepository[Commodity, String](db) with CommodityRepository {

}
