package wh.images.domain.model

import java.io.ByteArrayOutputStream
import java.net.URL

case class LazyImage(name: String, link: String) extends Image(LazyImageCreated(name, link)) {
  override def data: Array[Byte] = {
    val bytes = new ByteArrayOutputStream
    val input = new URL(link).openStream()
    Iterator.continually(input.read()).takeWhile(-1 !=).foreach(bytes.write)
    bytes.toByteArray
  }

  def download: Image = {
    apply(ImageDownloaded(name, link))
  }

  protected def when(imageDownloaded: ImageDownloaded): DownloadedImage = {
    imageDownloaded.initializedObject()
  }
}
