package wh.extractor

import java.net.URL

trait Extractor {
  def extract(url: URL): Iterator[ExtractedEntry]
}
