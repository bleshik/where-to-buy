package wh.application.extractor.infrastructure

object Environment extends Enumeration {
  type Environment = Value
  val DEV, PROD = Value

  def current = if ("production".equals(System.getenv("ENVIRONMENT"))) PROD else DEV

  def privateIp = Option(System.getenv("PRIVATE_IP"))

  def balancerIp = Option(System.getenv("BALANCER_IP"))

  def akkaEndpoint = Option(System.getenv("WH_API_AKKA_ENDPOINT")).getOrElse("akka.tcp://WhereToBuySystem@127.0.0.1:9000/user/ExtractedEntryHandler")

  def instance = Option(System.getenv("INSTANCE")).map(_.toInt).getOrElse(1)

  def instances = Option(System.getenv("INSTANCES")).map(_.toInt).getOrElse(1)

  def shops = Option(System.getenv("SHOPS")).map(_.split(',').toList).orElse(Some(List("Dixy")))

  def cities = Option(System.getenv("CITIES")).map(_.split(',').toList).getOrElse(List("Москва"))

  def minimumConcurrency = Option(System.getenv("CONCURRENCY")).map(_.toInt).getOrElse(Runtime.getRuntime.availableProcessors())

  def logDeadLetters = Option(System.getenv("LOG_DEAD_LETTERS")).map(_.toInt).getOrElse(10)

}
