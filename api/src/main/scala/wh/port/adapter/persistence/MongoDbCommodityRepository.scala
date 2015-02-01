package wh.port.adapter.persistence

import com.mongodb
import com.mongodb.{BasicDBObject, DB}
import com.mongodb.casbah.Imports._
import repository.eventsourcing.mongodb.MongoDbEventSourcedRepository
import wh.domain.model.{Commodity, CommodityRepository}
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

class MongoDbCommodityRepository(override val db: DB)
  extends MongoDbEventSourcedRepository[Commodity, String](db) with CommodityRepository {
  snapshots.createIndex(new BasicDBObject("kind", 1))
  snapshots.createIndex(new BasicDBObject("entries.shop", 1).append("entries.shopSpecificName", 1))

  private val matcher = new CommodityMatcher

  override def findSimilar(commodity: Commodity): Option[Commodity] = {
    get(commodity.name).orElse {
      commodity.entries.flatMap { e =>
        Option(snapshots.findOne(new BasicDBObject("entries.shop", e.shop).append("entries.shopSpecificName", e.shopSpecificName)))
      }.headOption.map(o => deserialize(o))
    }.orElse {
      commodity.entries.flatMap { e =>
        snapshots.find(
          new BasicDBObject("entries.shop", new BasicDBObject("$ne", e.shop))
            .append("kind", kind(e.shopSpecificName))
        ).asScala
        .map(r => deserialize(r))
        .find(r => matcher.matching(commodity, r))
      }.headOption
    }
  }

  private def kind(name: String): String = matcher.titleTokens(name, "").kind.toLowerCase

  override protected def serialize(entity: Commodity): mongodb.DBObject = {
    val dbObject = super.serialize(entity)
    dbObject.put("kind", kind(dbObject.get("name").asInstanceOf[String]))
    dbObject
  }
}
