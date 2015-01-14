package repository.eventsourcing

import repository.IdentifiedEntity

abstract class IdentifiedEventSourcedEntity[T <: EventSourcedEntity[T], K] extends EventSourcedEntity[T] with IdentifiedEntity[K] {

}
