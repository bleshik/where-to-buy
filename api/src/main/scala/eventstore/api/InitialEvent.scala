package eventstore.api

trait InitialEvent[T] extends Event {
  def initializedObject() : T
}
