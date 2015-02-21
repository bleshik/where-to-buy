package wh.application.extractor

import javax.inject.Inject

import akka.actor.Actor
import com.typesafe.scalalogging.LazyLogging
import wh.extractor.{ExtractedShop, Category, ExtractedEntry}
import wh.images.domain.model.{LazyImage, ImageRepository}
import wh.inventory.domain.model.{Shop, CommodityRepository, Commodity}

class EntryExtractingActor @Inject()(commodityRepository: CommodityRepository, imageRepository: ImageRepository)
  extends Actor with LazyLogging {

  override def receive: Receive = {
    case entry: ExtractedEntry =>
      logger.debug(s"Received entry $entry")
      def flatCategories(c: Category): Stream[Category] =
        c #:: (if (c.parentCategory != null) flatCategories(c.parentCategory) else Stream.empty)
      val categories = Option(entry.category).map(c =>
        flatCategories(c).flatMap(_.name.split("[,и&\\s]").map(_.trim.toLowerCase)).filter(t => t.size >= 4).toSet
      ).getOrElse(Set.empty)

      val incomingCommodity = Commodity.arrived(entry.shop, entry.name, entry.price, categories)
      val c = commodityRepository.findSimilar(incomingCommodity)
        .map { c =>
        logger.debug(s"Found for $entry: $c")
        if (c.entry(entry.shop).isDefined) {
          if (c.price(entry.shop).get != entry.price) {
            c.changePrice(entry.shop, entry.price)
          } else {
            c
          }
        } else {
          c.arrived(entry.shop, entry.name, entry.price, categories)
        }
      }.getOrElse(incomingCommodity)
      commodityRepository.save(c)

      if (!imageRepository.contains(c.name)) {
        imageRepository.save(LazyImage(c.name, entry.image.toString))
      } else if (c.entries.size > 1) {
        imageRepository.get(c.name).map {
          case img: LazyImage => imageRepository.save(img.download)
          case _ =>
        }
      }

      logger.debug(s"Commodity amount ${commodityRepository.size}")
  }

  private implicit def extractedShopToShop(extractedShop: ExtractedShop): Shop = {
    Shop(extractedShop.name, extractedShop.city)
  }
}