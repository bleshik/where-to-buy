package repository.eventsourcing.example.domain

import eventstore.api.Event

case class HouseBuilt(address: String, priceInCents: Long, owner: String) extends Event {

}
