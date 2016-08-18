package wh.application.extractor

import java.net.URL
import wh.extractor.domain.model.ExtractedEntry

case class ExtractCity(city: String, extract: Extract, attributes: Map[String, String] = Map()) {
  def withUrl(url: URL): ExtractCity = copy(extract = extract.copy(url = url))
}

