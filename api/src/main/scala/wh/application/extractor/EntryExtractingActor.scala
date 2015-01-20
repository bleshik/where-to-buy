package wh.application.extractor

import javax.inject.Inject

import akka.actor.Actor
import wh.domain.model.{Commodity, CommodityRepository}
import wh.extractor.ExtractedEntry

class EntryExtractingActor @Inject()(commodityRepository: CommodityRepository) extends Actor {
  override def receive: Receive = {
    case entry: ExtractedEntry =>
      commodityRepository.save(
        commodityRepository.get(entry.name)
          .map { c =>
            if (c.entryInShop(entry.name).isDefined) {
              c.changePrice(entry.source, entry.price)
            } else {
              c.arrived(entry.source, entry.name, entry.price)
            }
          }.getOrElse(Commodity.arrived(entry.source, entry.name, entry.price))
      )
  }
}