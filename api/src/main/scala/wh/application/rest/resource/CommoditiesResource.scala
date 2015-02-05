package wh.application.rest.resource

import javax.inject.Inject

import akka.actor.ActorRefFactory
import spray.http.Uri.Path
import spray.http.{ContentTypes, MediaRanges, MediaRange, HttpEntity}
import spray.http.MediaTypes._
import spray.httpx.marshalling.Marshaller
import spray.routing.Route
import wh.application.rest.AbstractRestComponent
import wh.images.domain.model.{Image, ImageRepository}
import wh.inventory.domain.model.{Commodity, CommodityRepository}
import spray.httpx.marshalling.BasicMarshallers._

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
      path(Segment) { name: String =>
        complete { commodityRepository.get(name) }
      } ~
      parameter("q") { query: String =>
        complete { commodityRepository.search(query) }
      }
    }
  }
}
