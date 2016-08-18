package wh.application.extractor

import java.net.URL
import wh.extractor.domain.model.ExtractedEntry
import wh.extractor.domain.model.Category

case class ExtractCategory(category: Category, extractCity: ExtractCity) {
  def withUrl(url: URL): ExtractCategory = copy(extractCity = extractCity.withUrl(url))
}

