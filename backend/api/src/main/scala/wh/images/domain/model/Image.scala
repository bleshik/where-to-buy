package wh.images.domain.model

import ddd.repository.AbstractIdentifiedEntity

abstract class Image(val name: String) extends AbstractIdentifiedEntity[String](name) {
  def data: Array[Byte]
}
