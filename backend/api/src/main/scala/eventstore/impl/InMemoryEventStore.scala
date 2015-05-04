package eventstore.impl

import java.util.ConcurrentModificationException

import eventstore.api.{Event, EventStore, EventStream}

import scala.collection.concurrent.TrieMap

class InMemoryEventStore extends EventStore {
  var streams: scala.collection.concurrent.Map[String, List[Event]] = new TrieMap[String, List[Event]]()

  override def close(): Unit = {}

  override def streamSince(streamName: String, lastReceivedEvent: Long): EventStream = {
    val allEvents = streams.getOrElse(streamName, List())
    val events = allEvents.takeRight(allEvents.length - lastReceivedEvent.toInt)
    new EventStream(allEvents.length, events)
  }

  override def append(streamName: String, currentVersion: Long, events: List[Event]): Unit = {
    // one global lock is enough for tests
    this.synchronized {
      val curEvents = streams.getOrElse(streamName, List())
      if (curEvents.length != currentVersion) {
        throw new ConcurrentModificationException()
      }
      streams.put(streamName, curEvents ::: events)
    }
  }

  override def streamNames: Set[String] = streams.keys.toSet

  override def version(streamName: String): Long = streams.get(streamName).map(_.size.asInstanceOf[Long]).getOrElse(-1L)
}
