package wh.application.extractor

import java.net.URL
import wh.extractor.domain.model.ExtractedEntry

case class ExtractRegion(region: String, extract: Extract, attributes: Map[String, String] = Map()) {
  def withUrl(url: URL): ExtractRegion = copy(extract = extract.copy(url = url))
}

