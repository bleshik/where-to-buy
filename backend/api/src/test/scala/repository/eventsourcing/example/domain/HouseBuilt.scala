package repository.eventsourcing.example.domain

import eventstore.api.InitialEvent

case class HouseBuilt(address: String, priceInCents: Long, owner: String) extends InitialEvent[House] {
  override def initializedObject(): House =
    House.build(address, priceInCents, owner)
}
