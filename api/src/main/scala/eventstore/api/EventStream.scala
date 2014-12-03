package eventstore.api

case class EventStream(version: Long, events: List[Event])

object EventStream {
  val empty = EventStream(0, List())
}
