package repository.eventsourcing.mongodb

import com.mongodb.{BasicDBObject, DB, DBObject}
import eventstore.impl.{MongoDbEventStore, MongoDbSerializer}
import repository.IdentifiedEntity
import repository.eventsourcing.{EventSourcedEntity, EventSourcedRepository}

import scala.collection.JavaConversions._

class MongoDbEventSourcedRepository[T <: EventSourcedEntity[T] with IdentifiedEntity[K]: Manifest, K] (val db: DB)
  extends EventSourcedRepository[T, K](new MongoDbEventStore(db.getCollection(manifest.runtimeClass.getSimpleName + "Events"))) {
  protected val snapshots = db.getCollection(manifest.runtimeClass.getSimpleName)
  private val serializer = new MongoDbSerializer[T]

  Migration {
    snapshots.createIndex(new BasicDBObject("id", 1), new BasicDBObject("unique", true))
    migrate()
  }

  override protected def saveSnapshot(entity: T): Unit = {
    snapshots.findAndModify(new BasicDBObject("version", entity.unmutatedVersion).append("id", entity.id), null, null, false, serialize(entity), false, entity.mutatedVersion == 1)
  }

  protected def serialize(entity: T): DBObject = {
    val dbObject = serializer.serialize(entity)
    dbObject.put("id", entity.id)
    dbObject.put("version", entity.mutatedVersion)
    dbObject
  }

  protected def deserialize(dbObject: DBObject): T = {
    val entity = serializer.deserialize(dbObject)
    if (dbObject.get("version") != null) {
      val versionField = classOf[EventSourcedEntity[T]].getDeclaredField("version")
      versionField.setAccessible(true)
      versionField.set(entity, dbObject.get("version").asInstanceOf[Long])
    }
    entity.commitChanges()
  }

  override protected def snapshot(id: K, before: Long): Option[T] = {
    val dbObject = snapshots.findOne(new BasicDBObject("version", new BasicDBObject("$lte", if (before < 0) Long.MaxValue else before)).append("id", id))
    if (dbObject != null) {
      Some(deserialize(dbObject))
    } else {
      None
    }
  }

  protected def migrate(): Unit = {}

  protected def find(query: DBObject, limit: Int = 100): Iterator[T] = {
    snapshots.find(query).limit(limit).iterator().map(deserialize)
  }

  protected def findOne(query: DBObject): Option[T] = {
    Option(snapshots.findOne(query)).map(deserialize)
  }
}
