package wh.application.extractor

import javax.inject.Inject

import akka.actor.Actor
import com.typesafe.scalalogging.LazyLogging
import wh.application.extractor.EntryExtractingActor.weird
import wh.extractor.domain.model.{Category, ExtractedEntry, ExtractedShop}
import wh.images.domain.model.{ImageRepository, LazyImage}
import wh.inventory.domain.model.{Commodity, CommodityRepository, Shop}

import scala.concurrent.{ExecutionContext, Future}

class EntryExtractingActor @Inject()(val commodityRepository: CommodityRepository, val imageRepository: ImageRepository, implicit val executionContext: ExecutionContext)
  extends Actor with LazyLogging {
  @volatile var i = 0

  override def receive: Receive = {
     case entry: ExtractedEntry =>
      Future {
        logger.trace(s"Received entry $entry")
        val start = System.currentTimeMillis()
        if (!commodityRepository.removed(entry.name)) {
          def flatCategories(c: Category): Stream[Category] =
            c #:: (if (c.parentCategory != null) flatCategories(c.parentCategory) else Stream.empty)
          val categories = Option(entry.category).map(c =>
            flatCategories(c).flatMap(_.name.split("[,и&\\s]").map(_.trim.toLowerCase)).filter(t => t.size >= 4).toSet
          ).getOrElse(Set.empty)

          val incomingCommodity = Commodity.arrived(entry.shop, entry.name, entry.price, categories)
          var isCommodityWeird = false
          val c = commodityRepository.findSimilar(incomingCommodity)
            .map { c =>
            logger.trace(s"Found for $entry: $c")
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
            imageRepository.save(LazyImage(c.name, entry.image.toString))
          } else if (c.entries.size > 1) {
            imageRepository.get(c.name).map {
              case img: LazyImage => imageRepository.save(img.download)
              case _ =>
            }
          }
        }
        i += 1
        if (i % 100 == 0) {
          logger.info(s"Entry was handled in ${System.currentTimeMillis() - start}")
        }
        logger.trace(s"Commodity amount ${commodityRepository.size}")
      }
  }

  private implicit def extractedShopToShop(extractedShop: ExtractedShop): Shop = {
    Shop(extractedShop.name, extractedShop.city)
  }
}

object EntryExtractingActor {
  def weird(pricesHistory: List[(Long, Long)]): Boolean =
    pricesHistory.size >= 4 && Stream.range(2, Math.min(pricesHistory.size / 2, 4) + 1).map(pricesHistory.map(_._2).grouped(_).toList).exists { g =>
      g.groupBy(identity).mapValues(_.size).values.toList.max > g.size / 2
    }
}