package wh.application.extractor

import akka.actor.Actor
import wh.extractor.ExtractedEntry

class EntryExtractingActor extends Actor {
  override def receive: Receive = {
    case entry: ExtractedEntry =>
      println(entry)
  }
}