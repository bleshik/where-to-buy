package wh.application.extractor.infrastructure

import actor.domain.model.Dispatcher
import actor.domain.model.EventTransport
import actor.port.adapter.local.LocalEventTransport
import actor.port.adapter.local.FilteringEventTransport
import wh.application.extractor.Extract
import wh.application.extractor.ExtractCategory
import wh.application.extractor.ExtractRegion
import scala.compat.java8.FunctionConverters._

object Environment extends Enumeration {
  type Environment = Value
  val DEV, PROD = Value

  def current = if ("production".equals(System.getenv("ENVIRONMENT"))) PROD else DEV

  def privateIp = Option(System.getenv("PRIVATE_IP"))

  def balancerIp = Option(System.getenv("BALANCER_IP"))

  def akkaEndpoint = Option(System.getenv("WH_API_AKKA_ENDPOINT")).getOrElse("akka.tcp://WhereToBuySystem@127.0.0.1:9000/user/EntryExtractingActor")

  def instance = Option(System.getenv("INSTANCE")).map(_.toInt).getOrElse(1)

  def instances = Option(System.getenv("INSTANCES")).map(_.toInt).getOrElse(1)

  def shops = Option(System.getenv("SHOPS")).map(_.split(',').toList)

  def cities = Option(System.getenv("CITIES")).map(_.split(',').toList).getOrElse(List("Москва"))

  def minimumConcurrency = Option(System.getenv("CONCURRENCY")).map(_.toInt).getOrElse(Runtime.getRuntime.availableProcessors())

  def logDeadLetters = Option(System.getenv("LOG_DEAD_LETTERS")).map(_.toInt).getOrElse(10)

  val dispatcher = new Dispatcher(new FilteringEventTransport(
    new LocalEventTransport(),
    { (event: EventTransport.Event)  =>
        if (event.payload.isInstanceOf[ExtractRegion]) {
          cities.contains(event.payload.asInstanceOf[ExtractRegion].region)
        } else if (event.payload.isInstanceOf[ExtractCategory]) {
          cities.contains(event.payload.asInstanceOf[ExtractCategory].extractRegion.region)
        } else { true }
    }.asJava))
}
