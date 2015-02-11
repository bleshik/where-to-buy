package wh.extractor

import java.net.URL

case class ExtractedEntry(source: String, name: String, price: Long, category: Category, image: URL = null)
