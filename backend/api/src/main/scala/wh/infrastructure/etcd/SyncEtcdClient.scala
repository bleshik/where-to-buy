package wh.infrastructure.etcd

import java.net.URI
import java.util.concurrent.TimeUnit

import com.typesafe.scalalogging.LazyLogging
import mousio.etcd4j.EtcdClient

import scala.collection.JavaConverters._
import scala.concurrent.duration.Duration

class SyncEtcdClient(etcd: URI, val duration: Duration = Duration(10, "s")) extends LazyLogging {
  val etcdClient: EtcdClient = new EtcdClient(etcd)

  def list(dir: String): Option[List[String]] = {
    try {
      Some(etcdClient.getDir(dir).timeout(duration.toMillis, TimeUnit.MILLISECONDS).send().get().node.nodes.asScala.map(e => e.key).toList)
    } catch {
      case e: Exception =>
        logger.error(s"Couldn't get a list $dir from etcd", e)
        None
    }
  }

  def get(key: String) : Option[String] = {
    try {
      Some(etcdClient.get(key).timeout(duration.toMillis, TimeUnit.MILLISECONDS).send().get().node.value)
    } catch {
      case e: Exception =>
        logger.error(s"Couldn't get a value $key from etcd", e)
        None
    }
  }
}
