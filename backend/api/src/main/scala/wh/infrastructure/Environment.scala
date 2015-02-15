package wh.infrastructure

import com.google.inject.Stage

object Environment extends Enumeration {
  type Environment = Value
  val DEV, PROD = Value

  def current = if ("production".equals(System.getenv("ENVIRONMENT"))) PROD else DEV

  def stage = if (current == PROD) Stage.PRODUCTION else Stage.DEVELOPMENT

  def defaultCoreOsDockerIp = "172.17.42.1"

  def etcdEndpoint = Option(System.getenv("ETCD_ENDPOINT")).getOrElse(s"http://$defaultCoreOsDockerIp:4001")

  def privateIp = Option(System.getenv("PRIVATE_IP"))
}
