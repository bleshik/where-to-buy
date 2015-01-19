package repository.eventsourcing.mongodb

import com.mongodb.DBCollection
import eventstore.impl.MongoDbEventStore
import repository.IdentifiedEntity
import repository.eventsourcing.{EventSourcedEntity, EventSourcedRepository}

class MongoDbEventSourcedRepository[T <: EventSourcedEntity[T] with IdentifiedEntity[K], K] (val events: DBCollection, val snapshots: DBCollection)
  extends EventSourcedRepository[T, K](new MongoDbEventStore(events)) {

}
