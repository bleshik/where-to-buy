package wh.extractor

import java.net.URL

case class ExtractedEntry(shop: ExtractedShop, name: String, price: Long, category: Category, image: URL = null)
