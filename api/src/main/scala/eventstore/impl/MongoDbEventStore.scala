package eventstore.impl

import java.util.ConcurrentModificationException

import com.mongodb._
import com.mongodb.casbah.Imports._
import com.novus.salat._
import eventstore.api.{Event, EventStore, EventStream}

import scala.collection.JavaConverters._

class MongoDbEventStore(val dbCollection: DBCollection) extends EventStore {
  implicit val ctx = new Context {
    val name = "When-Necessary-Context"

    override val typeHintStrategy = StringTypeHintStrategy(when = TypeHintFrequency.Always,
      typeHint = "_typeHint")

  }
  // unique index on "idx" field is required for thread safety
  dbCollection.createIndex(new BasicDBObject("streamId", 1).append("idx", 1), new BasicDBObject("unique", true))

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

  private def deserialize(mongoObject: DBObject): Event = {
    grater[Event].asObject(mongoObject)
  }

  private def serialize(event: Event, streamName: String): DBObject = {
    val dbObject = grater[Event].asDBObject(event)
    dbObject.put("occurredOn", System.nanoTime())
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
      case e: MongoException.DuplicateKey => throw new ConcurrentModificationException()
    }
  }

  override def streamNames: Set[String] = dbCollection.distinct("streamId").asInstanceOf[java.util.List[String]].asScala.toSet[String]
}