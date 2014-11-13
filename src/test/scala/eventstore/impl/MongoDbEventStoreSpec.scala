package eventstore.impl

import com.github.fakemongo.Fongo
import com.mongodb.DB
import eventstore.api.{Event, EventStore}

class MongoDbEventStoreSpec extends AbstractEventStoreSpec {
  private def newEventStore: EventStore = new MongoDbEventStore(db().getCollection("events"))
  private val complexEvent: ComplexEvent = ComplexEvent(123, "Yeah")

  it should "be thread safe" in {
    stressTest(newEventStore)
  }

  it should "handle complex events" in {
    val eventStore = newEventStore
    eventStore.append("complexEvents", 0, List(complexEvent))
    eventStore.stream("complexEvents").events.head should be(complexEvent)
  }

  it should "return all previously added streams" in {
    val eventStore = newEventStore
    eventStore.append("eventStream0", 0, List(complexEvent))
    eventStore.append("eventStream1", 0, List(complexEvent))
    eventStore.size should be(2)
    eventStore.streamNames should be(Set("eventStream0", "eventStream1"))
  }

  private def db(dbName: String = "Bills"): DB = {
    new Fongo(getClass.getCanonicalName).getDB(dbName)
  }
}

case class ComplexEvent(id: Int, name: String) extends Event
