package wh.images.domain.model

import java.io.ByteArrayOutputStream
import java.net.URL

import org.apache.commons.io.IOUtils

case class LazyImage(name: String, link: String) extends Image(LazyImageCreated(name, link)) {
  override def data: Array[Byte] = {
    val bytes = new ByteArrayOutputStream
    IOUtils.copy(new URL(link).openStream(), bytes)
    bytes.toByteArray
  }
}
