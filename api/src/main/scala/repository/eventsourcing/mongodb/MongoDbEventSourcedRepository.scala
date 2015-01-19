package repository.eventsourcing.mongodb

import com.mongodb.{BasicDBObject, DBCollection}
import eventstore.impl.{MongoDbEventStore, MongoDbSerializer}
import repository.IdentifiedEntity
import repository.eventsourcing.{EventSourcedEntity, EventSourcedRepository}

class MongoDbEventSourcedRepository[T <: EventSourcedEntity[T] with IdentifiedEntity[K]: Manifest, K] (val events: DBCollection, val snapshots: DBCollection)
  extends EventSourcedRepository[T, K](new MongoDbEventStore(events)) {
  private val serializer = new MongoDbSerializer[T]

  override protected def saveSnapshot(entity: T): Unit = {
    val dbObject = serializer.serialize(entity)
    dbObject.put("version", entity.unmutatedVersion)
    snapshots.save(dbObject)
  }

  override protected def snapshot(id: K, before: Long): Option[T] = {
    val dbObject = snapshots.findOne(new BasicDBObject("version", new BasicDBObject("$lte", if (before < 0) Long.MaxValue else before)).append("id", id))
    if (dbObject != null) {
      dbObject.removeField("version")
      Some(serializer.deserialize(dbObject))
    } else {
      None
    }
  }
}
