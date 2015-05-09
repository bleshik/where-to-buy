package wh.application.rest.resource

import javax.inject.Inject

import akka.actor.ActorRefFactory
import spray.http.HttpEntity
import spray.http.MediaTypes._
import spray.routing.Route
import wh.application.rest.AbstractRestComponent
import wh.images.domain.model.ImageRepository
import wh.inventory.domain.model.{Commodity, CommodityRepository}

import scala.concurrent.Future

class CommoditiesResource @Inject()(override val actorRefFactory: ActorRefFactory,
                                    val commodityRepository: CommodityRepository,
                                    val imageRepository: ImageRepository)
  extends AbstractRestComponent(actorRefFactory) {

  marshaller(`image/jpeg`, {
    case c: Commodity => HttpEntity(`image/jpeg`, imageRepository.get(c.name).map(_.data).get)
    case _ => null
  })

  override def doGetRoute: Route = pathPrefix("commodities") {
    get {
      pathPrefix(Segment) { name: String =>
        path("prices") { complete { Future { commodityRepository.averagePrices(name) } } } ~
        cacheImagesForDay { complete { Future { commodityRepository.get(name) } } }
      } ~
      parameter("q") { query: String =>
        parameter("limit") { limit: String =>
          parameter("offset") { offset: String =>
            complete { Future { commodityRepository.search(query,  "Москва", limit.toInt, offset.toInt) } }
          }
        }
      }
    }
  }
}
