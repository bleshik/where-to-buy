package wh.extractor.domain.model

import java.net.URL

trait Extractor {
  def extract(url: URL): Iterator[ExtractedEntry]
  def parts(url: URL): List[() => Iterator[ExtractedEntry]] = List(() => extract(url))
}
