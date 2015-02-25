package wh.port.adapter.persistence

import com.mongodb
import com.mongodb.DB
import com.mongodb.casbah.Imports._
import repository.eventsourcing.mongodb.MongoDbEventSourcedRepository
import wh.inventory.domain.model.{Shop, CommodityRepository, Commodity}

class MongoDbCommodityRepository(override val db: DB)
  extends MongoDbEventSourcedRepository[Commodity, String](db) with CommodityRepository {
  override def findSimilar(commodity: Commodity): Option[Commodity] = {
    get(commodity.name).orElse {
      commodity.entries.flatMap { e =>
        findOne(
          MongoDBObject(
            "entries.shop" -> MongoDBObject("name" -> e.shop.name, "city" -> e.shop.city),
            "entries.shopSpecificName" -> e.shopSpecificName
          )
        )
      }.headOption
    }.orElse {
      commodity.entries.flatMap { e =>
        find(
          MongoDBObject(
            "entries.shop" -> MongoDBObject("$ne" -> MongoDBObject("name" -> e.shop.name, "city" -> e.shop.city)),
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
  override def search(searchPattern: String, city: String): List[Commodity] = {
    if (searchPattern.isEmpty) {
      List()
    } else {
        find(MongoDBObject(
          "$or" -> List(
            MongoDBObject("kind" -> MongoDBObject("$regex" -> searchPattern.toLowerCase)),
            MongoDBObject("sanitizedName" -> MongoDBObject("$regex" -> searchPattern.toLowerCase))
          ),
          "relevantCities" -> city
        ), 20).toList
    }
  }

  override protected def migrate(): Unit = {
    snapshots.createIndex(MongoDBObject("sanitizedName" -> 1))
    snapshots.createIndex(MongoDBObject("kind" -> 1))
    snapshots.createIndex(MongoDBObject("entries.shop.name" -> 1, "entries.shop.city" -> 1, "entries.shopSpecificName" -> 1))
    snapshots.createIndex(MongoDBObject("relevantCities" -> 1))
  }

  private def kind(name: String): String = matcher.titleTokens(name, Shop("", "")).kind.toLowerCase

  override protected def serialize(entity: Commodity): mongodb.DBObject = {
    val dbObject = super.serialize(entity)
    dbObject.put("sanitizedName", matcher.sanitizeName(dbObject.get("name").asInstanceOf[String]))
    dbObject.put("kind", kind(dbObject.get("name").asInstanceOf[String]))
    dbObject.put("relevantCities", entity.entries.groupBy(_.shop.city).filter(_._2.size > 1).map(_._1).toSet)
    dbObject
  }

  private val matcher = new CommodityMatcher
}
