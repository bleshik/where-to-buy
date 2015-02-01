package wh.port.adapter.persistence

import com.mongodb.{BasicDBObject, DB}
import repository.eventsourcing.mongodb.MongoDbEventSourcedRepository
import wh.domain.model.{CommodityRepository, Commodity}
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import com.mongodb.casbah.Imports._

class MongoDbCommodityRepository(override val db: DB)
  extends MongoDbEventSourcedRepository[Commodity, String](db) with CommodityRepository {
  snapshots.createIndex(new BasicDBObject("name", "text"), new BasicDBObject("default_language", "ru"))

  override def get(shop: String, name: String, minScore: Double): Option[Commodity] = {
    val sanitizedName: String = name.filter({c => c.isLetterOrDigit || c.isSpaceChar || c.isWhitespace})
    snapshots.aggregate(List(
      new BasicDBObject("$match", new BasicDBObject("$text", new BasicDBObject("$search", sanitizedName).append("$language", "ru"))
        .append("entries.shop", new BasicDBObject("$ne", shop))),
      new BasicDBObject("$project", new BasicDBObject("score", new BasicDBObject("$meta", "textScore")).append("doc", "$$ROOT")),
      new BasicDBObject("$sort", new BasicDBObject("score", -1)),
      new BasicDBObject("$match", new BasicDBObject("score", new BasicDBObject("$gte", minScore.asInstanceOf[java.lang.Double]))),
      new BasicDBObject("$limit", 1)
    )).results().asScala.headOption.map(r => serializer.deserialize(r.get("doc").asInstanceOf[DBObject]))
  }

  
}
