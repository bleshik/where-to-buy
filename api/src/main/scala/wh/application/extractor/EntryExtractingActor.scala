package wh.application.extractor

import javax.inject.Inject

import akka.actor.Actor
import com.typesafe.scalalogging.LazyLogging
import wh.domain.model.{Commodity, CommodityRepository}
import wh.extractor.{Category, ExtractedEntry}

class EntryExtractingActor @Inject()(commodityRepository: CommodityRepository) extends Actor with LazyLogging {
  override def receive: Receive = {
    case entry: ExtractedEntry =>
      logger.debug(s"Received entry $entry")
      def flatCategories(c: Category): Stream[Category] =
        c #:: (if (c.parentCategory != null) flatCategories(c.parentCategory) else Stream.empty)
      val categories = Option(entry.category).map(c =>
        flatCategories(c).flatMap(_.name.split("[,и&\\s]").map(_.trim.toLowerCase)).filter(t => t.size >= 4).toSet
      ).getOrElse(Set.empty)

      val incomingCommodity = Commodity.arrived(entry.source, entry.name, entry.price, categories)
      val c = commodityRepository.findSimilar(incomingCommodity)
        .map { c =>
        logger.debug(s"Found for $entry: $c")
        if (c.entry(entry.source).isDefined) {
          if (c.price(entry.source).get != entry.price) {
            c.changePrice(entry.source, entry.price)
          } else {
            c
          }
        } else {
          c.arrived(entry.source, entry.name, entry.price, categories)
        }
      }.getOrElse(incomingCommodity)
      commodityRepository.save(c)
      logger.debug(s"Commodity amount ${commodityRepository.size}")
  }
}