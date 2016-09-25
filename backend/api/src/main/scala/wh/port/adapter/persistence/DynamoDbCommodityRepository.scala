package wh.port.adapter.persistence

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.document.Item
import com.amazonaws.services.dynamodbv2.document.KeyAttribute
import com.amazonaws.services.dynamodbv2.document.PrimaryKey
import com.amazonaws.services.dynamodbv2.document.QueryFilter
import com.amazonaws.services.dynamodbv2.document.RangeKeyCondition
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.xspec.ExpressionSpecBuilder
import com.amazonaws.services.dynamodbv2.xspec.ExpressionSpecBuilder._
import com.typesafe.scalalogging.LazyLogging
import ddd.repository.eventsourcing.dynamodb.DynamoDbEventSourcedRepository
import eventstore.PayloadEvent
import eventstore.util.dynamodb.DynamoDbObjectMapper
import eventstore.util.dynamodb.ExtendedTable
import eventstore.util.dynamodb.SimpleTable
import java.util.Arrays
import scala.collection.JavaConverters._
import scala.compat.java8.OptionConverters._
import wh.inventory.domain.model._

class DynamoDbCommodityRepository(val client: AmazonDynamoDB)
  extends DynamoDbEventSourcedRepository[Commodity, String](client, 25L, 25L, ScalaGsonDynamoDbObjectMapper())
  with CommodityRepository
  with LazyLogging {

  private val nameMappings = new SimpleTable[String](client, "CommodityNames", "shopSpecificName", classOf[String], provisionedThroughput)
  // TODO: use a search engine instead, e.g. ElasticSearch is a good fit for it
  private val commodityTokens = new ExtendedTable(
    client,
    "CommodityTokens",
    Arrays.asList(
      new AttributeDefinition("city", "S"), 
      new AttributeDefinition("token", "S")
    ),
    Arrays.asList(
      new KeySchemaElement("city", KeyType.HASH),
      new KeySchemaElement("token", KeyType.RANGE)
   ),
    provisionedThroughput
  )

  override protected def initializeTable(table: ExtendedTable): Unit = {
      table.createIfNotExists(
        Arrays.asList(new AttributeDefinition("kind", "S"), new AttributeDefinition("name", "S")),
        Arrays.asList(new KeySchemaElement("kind", KeyType.HASH), new KeySchemaElement("name", KeyType.RANGE)),
        provisionedThroughput
      )
  }

  override protected def toDbId(id: String): PrimaryKey = new PrimaryKey("kind", kind(id), "name", id)

  protected def when(arrived: CommodityArrived, commodity: Commodity): Unit = {
    nameMappings.put(arrived.name, "primaryName", commodity.name)
    val entriesFromSameCity = commodity.entries.filter(_.shop.city.equals(arrived.shop.city))
    // index commodities only with several entries in the city
    if (entriesFromSameCity.size > 1) {
      entriesFromSameCity.flatMap { e => extractTokens(e.shopSpecificName) }.foreach { token =>
        val item = 
        commodityTokens.putItem(
          new Item()
            .withString("city", arrived.shop.city)
            .withString("token", token + commodity.name.toLowerCase)
            .withString("name", commodity.name)
            .withString("nameInLowerCase", commodity.name.toLowerCase)
        )
      }
    }
  }

  override def findSimilar(commodity: Commodity): Option[Commodity] = {
    get(commodity.name).asScala.orElse(
      commodity.entries.flatMap { e =>
        Option(nameMappings.getItem(e.shopSpecificName)).map(_.getString("primaryName"));
      }.headOption.flatMap(get(_).asScala)
    ).orElse(
      commodity.entries.flatMap { e =>
        table.queryStream(
            new QuerySpec().withHashKey("kind", kind(e.shopSpecificName)),
            true
        ).iterator()
         .asScala
         .toList
         .map(mapper.mapToObject(_).asInstanceOf[Commodity])
         .filter(r => matcher.matching(commodity, r))
         .sortBy(r => matcher.matchingConfidence(commodity, r))
         .reverse
      }.headOption
    )
  }

  /**
   * Finds list of commodities using the search pattern.
   * @param searchPattern a pattern used for search.
   * @return list of commodities.
   */
  override def search(searchPattern: String, city: String, limit: Int, lastSeenCommodityName: Option[String]): List[Commodity] = {
    val tokens = extractTokens(searchPattern)
    val query = new QuerySpec().withHashKey("city", city)
      .withMaxResultSize(limit)
      .withRangeKeyCondition(new RangeKeyCondition("token").beginsWith(tokens.head))
    if (lastSeenCommodityName.nonEmpty) {
      query.withExclusiveStartKey("city", city, "name", lastSeenCommodityName.get)
    }
    if (tokens.tail.nonEmpty) {
      query.withQueryFilters(
        tokens.tail.map { t => new QueryFilter("nameInLowerCase").contains(t) }.toList:_*
      )
    }
    commodityTokens.queryStream(query).iterator().asScala.map(_.getString("name")).flatMap(get(_).asScala).toList
  }

  private def extractTokens(name: String): Set[String] =
    matcher.sanitizeName(name).split("\\s+").map(_.toLowerCase).distinct.toSet.filter(_.length > 2)

  private def kind(name: String): String = matcher.titleTokens(name, Shop("", "")).kind.toLowerCase

  override protected def serialize(entity: Commodity): Item =
    super.serialize(entity).withString("kind", kind(entity.name))

  private val matcher = new CommodityMatcher

  override def prices(commodityName: String, shop: Shop): Option[PricesHistory] = {
    eventStore.stream(streamName(commodityName)).asScala.map { stream =>
      PricesHistory(commodityName, stream.iterator().asScala.map {
        case e: PayloadEvent[AnyRef @unchecked] => (e.getOccurredOn, e.payload)
        case e => e
      }.filter {
        case (t: Long, e: CommodityArrived) => e.shop.equals(shop)
        case (t: Long, e: CommodityPriceChanged) => e.shop.equals(shop)
        case _ => false
      }.toList.map {
        case (t: Long, e: CommodityArrived) => (t, e.price)
        case (t: Long, e: CommodityPriceChanged) => (t, e.price)
      })
    }
  }

  override def averagePrices(commodityName: String, city: Option[String]): Option[PricesHistory] = {
    eventStore.stream(streamName(commodityName)).asScala.map { stream =>
      PricesHistory(commodityName, stream.iterator().asScala.map {
        case e: PayloadEvent[AnyRef @unchecked] => (e.getOccurredOn, e.payload)
        case e => e
      }.filter {
        case (t: Long, e: CommodityArrived) => !city.exists(!_.equals(e.shop.city))
        case (t: Long, e: CommodityPriceChanged) => !city.exists(!_.equals(e.shop.city))
        case _ => false
      }.toList.groupBy {
        case (t: Long, e: CommodityArrived) => e.shop
        case (t: Long, e: CommodityPriceChanged) => e.shop
        case _ => false
      }.mapValues {
        _.map {
          case (t: Long, e: CommodityArrived) => (t, List(e.price))
          case (t: Long, e: CommodityPriceChanged) => (t, List(e.price))
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
