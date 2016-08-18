package wh.extractor.domain.model

import java.net.URL

trait Extractor {
  def extract(url: URL, callback: (ExtractedEntry) => Unit): Unit
}
