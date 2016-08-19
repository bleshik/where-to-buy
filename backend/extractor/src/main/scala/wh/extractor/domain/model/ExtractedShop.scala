package wh.extractor.domain.model

case class ExtractedShop(name: String, region: ExtractedRegion) {
  def this(name: String, region: String) {
    this(name, ExtractedRegion(region))
  }

  def city = region.city
}
