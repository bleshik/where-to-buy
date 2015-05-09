package repository.eventsourcing

import eventstore.api.Event

case class RemovedEvent[K](id: K) extends Event
