package wh.extractor

case class ExtractedEntry(source: String, name: String, price: Long, category: Category, image: Option[Array[Byte]] = None)
