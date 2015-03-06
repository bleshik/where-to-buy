package wh.port.adapter.persistence

import com.mongodb
import com.mongodb.DB
import com.mongodb.casbah.Imports._
import com.typesafe.scalalogging.LazyLogging
import repository.eventsourcing.mongodb.MongoDbEventSourcedRepository
import wh.inventory.domain.model.{Shop, CommodityRepository, Commodity}

class MongoDbCommodityRepository(override val db: DB)
  extends MongoDbEventSourcedRepository[Commodity, String](db) with CommodityRepository with LazyLogging {

  override def findSimilar(commodity: Commodity): Option[Commodity] = {
    commodity.entries.flatMap { e =>
      findOne(MongoDBObject("$or" -> List(
        MongoDBObject("entries.shopSpecificName" -> e.shopSpecificName),
        MongoDBObject("id" -> e.shopSpecificName)
      )))
    }.headOption.orElse {
      commodity.entries.flatMap { e =>
        find(
          MongoDBObject(
            "entries.shop.name" -> MongoDBObject("$ne" -> e.shop.name),
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
  override def search(searchPattern: String, city: String, limit: Int, offset: Int): List[Commodity] = {
    if (searchPattern.isEmpty) {
      List()
    } else {
      find(MongoDBObject(
        "$and" -> matcher.sanitizeName(searchPattern).split("\\s+").distinct.toList.map(token => MongoDBObject("nameTokens" -> MongoDBObject("$regex" -> s"^$token.*"))),
        "relevantCities" -> city
      ), MongoDBObject("name" -> 1), limit, offset).toList
    }
  }

  override protected def migrate(): Unit = {
    snapshots.createIndex(MongoDBObject("nameTokens" -> 1))
    snapshots.createIndex(MongoDBObject("kind" -> 1))
    snapshots.createIndex(MongoDBObject("entries.shop.name" -> 1))
    snapshots.createIndex(MongoDBObject("entries.shopSpecificName" -> 1))
    snapshots.createIndex(MongoDBObject("relevantCities" -> 1))
  }

  private def kind(name: String): String = matcher.titleTokens(name, Shop("", "")).kind.toLowerCase

  override protected def serialize(entity: Commodity): mongodb.DBObject = {
    val dbObject = super.serialize(entity)
    dbObject.put("kind", kind(dbObject.get("name").asInstanceOf[String]))
    dbObject.put("relevantCities", entity.entries.groupBy(_.shop.city).filter(_._2.size > 1).map(_._1).toSet)
    dbObject.put("nameTokens", dbObject.get("entries").asInstanceOf[MongoDBList].flatMap { entry =>
      matcher.sanitizeName(entry.asInstanceOf[DBObject].get("shopSpecificName").asInstanceOf[String]).split("\\s+")
    }.toSet.filter(_.size > 2))
    dbObject
  }

  private val matcher = new CommodityMatcher
}
