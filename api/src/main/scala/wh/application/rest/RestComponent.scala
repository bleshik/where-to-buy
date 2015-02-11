package wh.application.rest

import spray.routing._

trait RestComponent extends HttpService {
  def route: Route
}
