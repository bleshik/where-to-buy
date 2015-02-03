package wh.application.rest.resource

import javax.inject.Inject

import akka.actor.ActorRefFactory
import spray.routing.Route
import wh.application.rest.AbstractRestComponent
import wh.domain.model.CommodityRepository

class CommoditiesResource @Inject()(override val actorRefFactory: ActorRefFactory, val commodityRepository: CommodityRepository)
  extends AbstractRestComponent(actorRefFactory) {
  override def route: Route = pathPrefix("commodities") {
    get {
      parameter("q") { query: String =>
        complete { commodityRepository.search(query) }
      }
    }
  }
}
