package wh.images.domain.model

case class DownloadedImage(name: String, link: String, data: Array[Byte]) extends Image(ImageDownloaded(name, link))
