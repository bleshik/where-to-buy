package eventstore.impl

import eventstore.api.{Event, EventStore}

class MongoDbEventStoreSpec extends AbstractEventStoreSpec with MongoDbSpec {
  private def newEventStore: EventStore = new MongoDbEventStore(db().getCollection("events"))
  private val complexEvent: ComplexEvent = ComplexEvent(123, "Yeah")

  it should "be thread safe" in {
    stressTest(newEventStore)
  }

  it should "handle complex events" in {
    val eventStore = newEventStore
    eventStore.append("complexEvents", List(complexEvent))
    eventStore.stream("complexEvents").get.events.head should be(complexEvent)
  }

  it should "return all previously added streams" in {
    val eventStore = newEventStore
    eventStore.append("eventStream0", 0, List(complexEvent))
    eventStore.append("eventStream1", 0, List(complexEvent))
    eventStore.size should be(2)
    eventStore.streamNames should be(Set("eventStream0", "eventStream1"))
  }
}

case class ComplexEvent(id: Int, name: String) extends Event
