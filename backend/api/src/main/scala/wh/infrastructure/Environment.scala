package wh.infrastructure

import java.net.URI

import com.google.inject.Stage

object Environment extends Enumeration {
  type Environment = Value
  val DEV, PROD = Value

  def current = if ("production".equals(System.getenv("ENVIRONMENT"))) PROD else DEV

  def stage = if (current == PROD) Stage.PRODUCTION else Stage.DEVELOPMENT

  def defaultCoreOsDockerIp = "172.17.42.1"

  def etcdEndpoint = Option(URI.create(System.getenv("ETCD_ENDPOINT"))).getOrElse(URI.create(s"http://$defaultCoreOsDockerIp:4001"))

  def privateIp = Option(System.getenv("PRIVATE_IP"))

  def balancerIp = Option(System.getenv("BALANCER_IP"))
}
