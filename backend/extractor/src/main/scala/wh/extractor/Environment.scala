package wh.extractor

object Environment extends Enumeration {
  type Environment = Value
  val DEV, PROD = Value

  def current = if ("production".equals(System.getenv("ENVIRONMENT"))) PROD else DEV

  def privateIp = Option(System.getenv("PRIVATE_IP"))

  def akkaEndpoint = Option(System.getenv("WH_API_AKKA_ENDPOINT")).getOrElse("127.0.0.1:9000")

  def instance = Option(System.getenv("INSTANCE").toInt).getOrElse(1)

  def instances = Option(System.getenv("INSTANCES").toInt).getOrElse(1)
}
