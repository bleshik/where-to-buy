package wh.application.rest

import javax.inject.Inject

import spray.routing.HttpServiceActor

class RestActor @Inject()(components: Set[RestComponent]) extends HttpServiceActor {
  def receive = runRoute(components.map(_.route).reduce(_~_))
}
