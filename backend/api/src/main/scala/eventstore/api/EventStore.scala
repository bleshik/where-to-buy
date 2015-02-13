package eventstore.api

trait EventStore {
  def close()
  def streamSince(streamName: String, lastReceivedEvent: Long): EventStream
  def stream(streamName: String): EventStream = streamSince(streamName, 0)
  def append(streamName: String, currentVersion: Long, events: List[Event]): Unit
  def contains(streamName: String): Boolean = stream(streamName).events.nonEmpty
  def size: Long = streamNames.size
  def streamNames: Set[String]
}