package wh.images.domain.model

case class InMemoryImage(override val name: String, link: String, data: Array[Byte]) extends Image(name) {
  def uploadedToS3(s3Url: String): ImageLink = ImageLink(name, s3Url)
}
