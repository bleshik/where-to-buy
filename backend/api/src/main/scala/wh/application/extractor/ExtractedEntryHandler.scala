package wh.application.extractor

import javax.inject.Inject

import com.typesafe.scalalogging.LazyLogging
import wh.application.extractor.ExtractedEntryHandler.weird
import wh.extractor.domain.model.{Category, ExtractedEntry, ExtractedShop}
import wh.images.domain.model.{ImageRepository, ImageLink}
import wh.inventory.domain.model.{Commodity, CommodityRepository, Shop}

import scala.concurrent.{ExecutionContext, Future}
import scala.compat.java8.OptionConverters._

class ExtractedEntryHandler @Inject()(val commodityRepository: CommodityRepository,
                                     val imageRepository: ImageRepository) extends LazyLogging {
  @volatile var i = 0

  def handle(entry: ExtractedEntry): Unit = {
      logger.debug(s"Received entry $entry")
      if (entry.shop.city.isDefined) {
        val start = System.currentTimeMillis()
        if (!commodityRepository.removed(entry.name)) {
          def flatCategories(c: Category): Stream[Category] =
            c #:: (if (c.parentCategory != null) flatCategories(c.parentCategory) else Stream.empty)
          val categories = Option(entry.category).map(c =>
              flatCategories(c).flatMap(_.name.split("[,и&\\s]").map(_.trim.toLowerCase)).filter(t => t.size >= 4).toSet
              ).getOrElse(Set.empty)

          val incomingCommodity = new Commodity(entry.shop, entry.name, entry.price, categories)
          var isCommodityWeird = false
          val c = commodityRepository.findSimilar(incomingCommodity)
            .map { c =>
                if (c.entry(entry.shop).isDefined) {
                  if (!c.price(entry.shop).get.equals(entry.price)) {
                    isCommodityWeird = commodityRepository.prices(c.name, entry.shop).exists { p => weird(p.history :+ (System.currentTimeMillis(), entry.price)) }
                    c.changePrice(entry.shop, entry.price)
                  } else {
                    c
                  }
                } else {
                  c.arrived(entry.shop, entry.name, entry.price, categories)
                }
            }.getOrElse(incomingCommodity)

            if (isCommodityWeird) {
              logger.info(s"Detected a weird commodity ${c.id}. Removing it...")
              commodityRepository.remove(c.id)
            } else {
              commodityRepository.save(c)
            }

            if (!imageRepository.contains(c.name)) {
              imageRepository.save(ImageLink(c.name, entry.image.toString).download)
            } else if (c.entries.size > 1) {
              imageRepository.get(c.name).asScala.map {
                case img: ImageLink =>
                  if (img.link.startsWith("http")) {
                    imageRepository.save(img.download)
                  }
                case _ =>
              }
            }
        }
        i += 1
        if (i % 20 == 0) {
          logger.info(s"Entry was handled in ${System.currentTimeMillis() - start}")
          logger.info(s"Commodity amount ${commodityRepository.size}")
        }
      }
  }

  private implicit def extractedShopToShop(extractedShop: ExtractedShop): Shop = {
    Shop(extractedShop.name, extractedShop.city.get)
  }
}

object ExtractedEntryHandler {
  def weird(pricesHistory: List[(Long, Long)]): Boolean =
    pricesHistory.size >= 4 && pricesHistory.map(_._2).distinct.size <= pricesHistory.size / 2
}
