package repository.eventsourcing.example.domain

import repository.IdentifiedEntity
import repository.eventsourcing.EventSourcedEntity

case class House private (id: String, address: String, priceInCents: Long, owner: String, destroyed: Boolean) extends EventSourcedEntity[House] with IdentifiedEntity[String] {

  def this(address: String, priceInCents: Long, owner: String) {
    this(address, address, priceInCents, owner, false)
    apply(HouseBuilt(address, priceInCents, owner))
  }

  def buy(newOwner: String): House = {
    apply(HouseBought(newOwner))
  }

  def destroy(): House = {
    apply(HouseDestroyed())
  }

  protected def this(event: HouseBuilt) {
    this(event.address, event.priceInCents, event.owner)
  }

  protected def when(event: HouseBought): House = {
    if (destroyed) {
      throw new IllegalStateException("You cannot buy a destroyed house")
    }
    copy(owner = event.newOwner)
  }

  protected def when(event: HouseDestroyed): House = {
    copy(destroyed = true)
  }
}
