package eventstore.impl

import java.util
import java.util.ConcurrentModificationException

import com.mongodb._
import eventstore.api.{Event, EventStore, EventStream}
import repository.eventsourcing.mongodb.Migration

import scala.collection.JavaConverters._

class MongoDbEventStore(val dbCollection: DBCollection) extends EventStore {
  Migration {
    // unique index on "idx" field is required for thread safety
    dbCollection.createIndex(new BasicDBObject("streamId", 1).append("idx", 1), new BasicDBObject("unique", true))
    dbCollection.createIndex(new BasicDBObject("occurredOn", 1))
  }

  private val serializer = new MongoDbSerializer[Event]

  override def close(): Unit = {}

  override def streamSince(streamName: String, lastReceivedEvent: Long): EventStream = {
    val mongoEvents =
      dbCollection.find(new BasicDBObject("streamId", streamName).append("idx", new BasicDBObject("$gt", lastReceivedEvent)))
        .sort(new BasicDBObject("occurredOn", 1))
        .toArray
        .asScala
        .toList
    val deserializedEvents: List[Event] = mongoEvents.map(mongoObject => deserialize(mongoObject))
    if (mongoEvents.isEmpty) {
      EventStream.empty
    } else {
      EventStream(mongoEvents.last.get("idx").asInstanceOf[Long], deserializedEvents)
    }
  }

  def deserialize(mongoObject: DBObject): Event = {
    val event = serializer.deserialize(mongoObject)
    val occurredOn = classOf[Event].getDeclaredField("_occurredOn")
    occurredOn.setAccessible(true)
    occurredOn.setLong(event, mongoObject.get("occurredOn").asInstanceOf[Long])
    event
  }

  def serialize(event: Event, streamName: String): DBObject = {
    val dbObject = serializer.serialize(event)
    dbObject.put("occurredOn", System.currentTimeMillis())
    dbObject.put("streamId", streamName)
    dbObject
  }

  override def append(streamName: String, currentVersion: Long, events: List[Event]): Unit = {
    try {
      val builder = dbCollection.initializeOrderedBulkOperation()
      var nextEventIndex = currentVersion + 1
      events.foreach(event => {
        val dbObject = serialize(event, streamName)
        dbObject.put("idx", nextEventIndex)
        builder.insert(dbObject)
        nextEventIndex += 1
      })
      builder.execute()
    } catch {
      case e: BulkWriteException =>
        if (e.getWriteErrors.asScala.exists(_.getCode == 11000)) {
          throw new ConcurrentModificationException(e)
        } else {
          throw e
        }
      case e: com.mongodb.MongoException.DuplicateKey  => throw new ConcurrentModificationException(e)
    }
  }

  override def streamNames: Set[String] = dbCollection.distinct("streamId").asInstanceOf[java.util.List[String]].asScala.toSet[String]

  override def version(streamName: String): Long = dbCollection.aggregate(new util.ArrayList[DBObject](){{
    add(new BasicDBObject("$match", new BasicDBObject("streamId", streamName)))
    add(new BasicDBObject("$sort", new BasicDBObject("idx", -1)))
    add(new BasicDBObject("$limit", 1))
  }}).results()
    .iterator()
    .next()
    .get("idx")
    .asInstanceOf[Long]
}
