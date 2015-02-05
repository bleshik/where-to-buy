package wh.port.adapter.persistence

import com.mongodb
import com.mongodb.DB
import com.mongodb.casbah.Imports._
import repository.eventsourcing.mongodb.MongoDbEventSourcedRepository
import wh.inventory.domain.model.{CommodityRepository, Commodity}

class MongoDbCommodityRepository(override val db: DB)
  extends MongoDbEventSourcedRepository[Commodity, String](db) with CommodityRepository {
  override def findSimilar(commodity: Commodity): Option[Commodity] = {
    get(commodity.name).orElse {
      commodity.entries.flatMap { e =>
        findOne(
          MongoDBObject(
            "entries.shop" -> e.shop,
            "entries.shopSpecificName" -> e.shopSpecificName
          )
        )
      }.headOption
    }.orElse {
      commodity.entries.flatMap { e =>
        find(
          MongoDBObject(
            "entries.shop" -> MongoDBObject("$ne" -> e.shop),
            "kind" -> kind(e.shopSpecificName)
          )
        ).find(r => matcher.matching(commodity, r))
      }.headOption
    }
  }

  /**
   * Finds list of commodities using the search pattern.
   * @param searchPattern a pattern used for search.
   * @return list of commodities.
   */
  override def search(searchPattern: String): List[Commodity] = {
    find(MongoDBObject(
      "name" -> MongoDBObject("$regex" -> searchPattern)//,
      //"entriesLength" -> MongoDBObject("$gt" -> 1)
    )).toList
  }

  snapshots.createIndex(MongoDBObject("kind" -> 1))
  snapshots.createIndex(MongoDBObject("entries.shop" -> 1, "entries.shopSpecificName" -> 1))

  private def kind(name: String): String = matcher.titleTokens(name, "").kind.toLowerCase

  override protected def serialize(entity: Commodity): mongodb.DBObject = {
    val dbObject = super.serialize(entity)
    dbObject.put("kind", kind(dbObject.get("name").asInstanceOf[String]))
    dbObject.put("entriesLength", entity.entries.size)
    dbObject
  }

  private val matcher = new CommodityMatcher
}
