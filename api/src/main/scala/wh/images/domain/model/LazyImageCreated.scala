package wh.images.domain.model

import eventstore.api.InitialEvent

case class LazyImageCreated(name: String, link: String) extends InitialEvent[LazyImage] {
  override def initializedObject(): LazyImage = new LazyImage(name, link)
}
