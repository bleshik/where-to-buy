package repository.eventsourcing

import eventstore.api.InitialEvent
import repository.IdentifiedEntity

abstract class IdentifiedEventSourcedEntity[T <: EventSourcedEntity[T], K](override val initialEvent: InitialEvent[T])
  extends EventSourcedEntity[T](initialEvent)
  with IdentifiedEntity[K] {

}
