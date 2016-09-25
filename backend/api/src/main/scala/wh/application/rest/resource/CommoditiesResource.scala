package wh.application.rest.resource

import akka.actor.ActorRefFactory
import javax.inject.Inject
import scala.compat.java8.OptionConverters._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Failure}
import spray.http.HttpEntity
import spray.http.MediaTypes._
import spray.http.StatusCodes
import spray.http.HttpResponse
import spray.routing.Route
import wh.application.commodity.CommodityService
import wh.application.rest.AbstractRestComponent
import wh.images.domain.model.ImageLink
import wh.images.domain.model.ImageRepository
import wh.inventory.domain.model.{Commodity, CommodityRepository}

class CommoditiesResource @Inject()(override val actorRefFactory: ActorRefFactory,
                                    val commodityRepository: CommodityRepository,
                                    val imageRepository: ImageRepository,
                                    val commodityService: CommodityService,
                                    implicit val executionContext: ExecutionContext)
  extends AbstractRestComponent(actorRefFactory) {

  override def doGetRoute: Route = pathPrefix("commodities") {
    parameter("city" ? "Москва") { city: String =>
      get {
        path("prices") { complete { Future { commodityRepository.averagePrices(commodityService.randomInterestingCommodity(city).name, Some(city)) } } } ~
        pathPrefix(Segment) { name: String =>
          path("prices") { complete { Future { commodityRepository.averagePrices(name, Some(city)) } } } ~
          cacheImagesForDay { onComplete(Future { imageRepository.get(name).asScala }) {
            case Success(img: Option[ImageLink]) =>
              if (img.get.link.startsWith("http")) {
                redirect(img.get.link, StatusCodes.TemporaryRedirect)
              } else {
                complete(HttpResponse(entity = HttpEntity(`image/jpeg`, img.get.data)))
              }
            case Failure(_) => complete(StatusCodes.NotFound)
          }}
        } ~
        parameter("q") { query: String =>
          parameter("limit") { limit: String =>
            parameter('last?) { last: Option[String] =>
              complete { Future { commodityRepository.search(query, city, limit.toInt, last) } }
            }
          }
        }
      }
    }
  }
}
