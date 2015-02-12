package wh.images.domain.model

import eventstore.api.InitialEvent
import repository.eventsourcing.IdentifiedEventSourcedEntity

abstract class Image(override val initialEvent: InitialEvent[Image])
  extends IdentifiedEventSourcedEntity[Image, String](initialEvent) {
  def id = name
  def name: String
  def data: Array[Byte]
}
