package wh.images.domain.model

import eventstore.api.InitialEvent

case class ImageDownloaded(name: String, link: String) extends InitialEvent[DownloadedImage] {
  override def initializedObject(): DownloadedImage = {
    DownloadedImage(name, link, LazyImage(name, link).data)
  }
}
