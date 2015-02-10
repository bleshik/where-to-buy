package wh.port.adapter.persistence

import java.net.URL

import com.mongodb.{DBObject, DB}
import repository.eventsourcing.mongodb.MongoDbEventSourcedRepository
import wh.images.domain.model.{LazyImage, Image, ImageRepository}

class MongoDbImageRepository(override val db: DB)
  extends MongoDbEventSourcedRepository[Image, String](db) with ImageRepository {

  override def get(id: String): Option[Image] = {
    super.get(id).map {
      case image: LazyImage => save(image.download)
      case image => image
    }
  }

  override protected def deserialize(dbObject: DBObject): Image = {
    if (dbObject.containsField("link")) {
      dbObject.put("link", new URL(dbObject.get("link").asInstanceOf[String]))
    }
    super.deserialize(dbObject)
  }

  override protected def serialize(entity: Image): DBObject = {
    val dbObject = super.serialize(entity)
    if (dbObject.containsField("link")) {
      dbObject.put("link", dbObject.get("link").toString)
    }
    dbObject
  }
}
