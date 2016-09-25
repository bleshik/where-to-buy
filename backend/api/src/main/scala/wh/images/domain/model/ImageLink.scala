package wh.images.domain.model

import java.io.ByteArrayOutputStream
import java.net.URL

case class ImageLink(override val name: String, link: String) extends Image(name) {

  override def data: Array[Byte] = {
    val bytes = new ByteArrayOutputStream
    val input = new URL(link).openStream()
    Iterator.continually(input.read()).takeWhile(-1 !=).foreach(bytes.write)
    bytes.toByteArray
  }

  def download: InMemoryImage = InMemoryImage(name, link, data)

}
