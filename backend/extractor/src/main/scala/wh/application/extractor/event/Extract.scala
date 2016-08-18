package wh.application.extractor

import java.net.URL
import wh.extractor.domain.model.ExtractedEntry

case class Extract(url: URL, callback: (ExtractedEntry) => Unit)

