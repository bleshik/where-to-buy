package wh.port.adapter.persistence

import com.mongodb
import com.mongodb.DB
import com.mongodb.casbah.Imports._
import com.typesafe.scalalogging.LazyLogging
import repository.eventsourcing.mongodb.MongoDbEventSourcedRepository
import wh.inventory.domain.model._

class MongoDbCommodityRepository(override val db: DB)
  extends MongoDbEventSourcedRepository[Commodity, String](db) with CommodityRepository with LazyLogging {

  override def findSimilar(commodity: Commodity): Option[Commodity] = {
    commodity.entries.flatMap { e =>
      findOne(MongoDBObject("$or" -> List(
        MongoDBObject("entries.shopSpecificName" -> e.shopSpecificName),
        MongoDBObject("id" -> e.shopSpecificName)
      )))
    }.headOption.orElse {
      commodity.entries.flatMap { e =>
        find(
          MongoDBObject(
            "entries.shop.name" -> MongoDBObject("$ne" -> e.shop.name),
            "kind" -> kind(e.shopSpecificName)
          ),
          limit = 0
        ).toList
         .filter(r => matcher.matching(commodity, r))
         .sortBy(r => matcher.matchingConfidence(commodity, r))
         .reverse
      }.headOption
    }
  }

  /**
   * Finds list of commodities using the search pattern.
   * @param searchPattern a pattern used for search.
   * @return list of commodities.
   */
  override def search(searchPattern: String, city: String, limit: Int, offset: Int): List[Commodity] = {
    find(MongoDBObject(
      "$and" -> matcher.sanitizeName(searchPattern).split("\\s+").distinct.toList.map(token => MongoDBObject("nameTokens" -> MongoDBObject("$regex" -> s"^$token.*"))),
      "relevantCities" -> city
    ), MongoDBObject("citiesRelevancy" -> -1, "name" -> 1), limit, offset).toList
  }

  override protected def migrate(): Unit = {
    snapshots.createIndex(MongoDBObject("nameTokens" -> 1))
    snapshots.createIndex(MongoDBObject("kind" -> 1))
    snapshots.createIndex(MongoDBObject("entries.shop.name" -> 1))
    snapshots.createIndex(MongoDBObject("entries.shopSpecificName" -> 1))
    snapshots.createIndex(MongoDBObject("relevantCities" -> 1))
    snapshots.createIndex(MongoDBObject("citiesRelevancy" -> -1))
  }

  private def kind(name: String): String = matcher.titleTokens(name, Shop("", "")).kind.toLowerCase

  override protected def serialize(entity: Commodity): mongodb.DBObject = {
    val dbObject = super.serialize(entity)
    dbObject.put("kind", kind(dbObject.get("name").asInstanceOf[String]))
    val relevantCities = entity.entries.groupBy(_.shop.city).map(e => (e._1, e._2.size)).filter(_._2 > 1)
    if (relevantCities.nonEmpty) {
      dbObject.put("relevantCities", relevantCities.keySet)
      dbObject.put("citiesRelevancy", relevantCities.values.max)
    }
    dbObject.put("nameTokens", dbObject.get("entries").asInstanceOf[MongoDBList].flatMap { entry =>
      matcher.sanitizeName(entry.asInstanceOf[DBObject].get("shopSpecificName").asInstanceOf[String]).split("\\s+")
    }.toSet.filter(_.size > 2))
    dbObject
  }

  private val matcher = new CommodityMatcher

  override def prices(commodityName: String, shop: Shop): Option[PricesHistory] = {
    eventStore.stream(streamName(commodityName)).map { stream =>
      PricesHistory(commodityName, stream.events.filter {
        case e: CommodityArrived => e.shop.equals(shop)
        case e: CommodityPriceChanged => e.shop.equals(shop)
        case _ => false
      }.map {
        case e: CommodityArrived => (e.occurredOn, e.price)
        case e: CommodityPriceChanged => (e.occurredOn, e.price)
      })
    }
  }

  override def averagePrices(commodityName: String, city: Option[String]): Option[PricesHistory] = {
    eventStore.stream(streamName(commodityName)).map { stream =>
      PricesHistory(commodityName, stream.events.filter {
        case e: CommodityArrived => !city.exists(!_.equals(e.shop.city))
        case e: CommodityPriceChanged => !city.exists(!_.equals(e.shop.city))
        case _ => false
      }.groupBy {
        case e: CommodityArrived => e.shop
        case e: CommodityPriceChanged => e.shop
        case _ => false
      }.mapValues {
        _.map {
          case e: CommodityArrived => (e.occurredOn, List(e.price))
          case e: CommodityPriceChanged => (e.occurredOn, List(e.price))
        }
      }.values
        .reduce((a, b) => aggregatePrices(a, b))
        .map(price => (price._1, price._2.sum / price._2.length)))
    }
  }

  private def aggregatePrices(a: List[(Long, List[Long])],
                            b: List[(Long, List[Long])],
                            aPrice: (Long, List[Long]) = null,
                            bPrice: (Long, List[Long]) = null): List[(Long, List[Long])] = {
    if (a.isEmpty) { b }
    else if (b.isEmpty) { a }
    else {
      if (a.head._1 > b.head._1) { aggregatePrices(b, a, bPrice, aPrice) }
      else {
        (a.head._1, if (bPrice != null) a.head._2 ++ bPrice._2 else a.head._2) +: aggregatePrices(a.tail, b, a.head, bPrice)
      }
    }
  }
}
