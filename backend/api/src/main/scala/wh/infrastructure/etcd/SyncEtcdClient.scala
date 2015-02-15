package wh.infrastructure.etcd

import com.typesafe.scalalogging.LazyLogging
import net.nikore.etcd.EtcdClient

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class SyncEtcdClient(val etcdClient: EtcdClient, val duration: Duration = Duration(10, "s")) extends LazyLogging {

  def list(dir: String, defaultValues: List[String]): List[String] = {
    try {
      Await.result(etcdClient.listDir(dir), duration).node.nodes.map(o => o.map(e => e.key)).getOrElse(defaultValues)
    } catch {
      case e: Exception =>
        logger.error(s"Couldn't get a list $dir from etcd", e)
        defaultValues
    }
  }

  def get(key: String, defaultValue: String) : String = {
    try {
      Await.result(etcdClient.getKey(key), duration).node.value.getOrElse(defaultValue)
    } catch {
      case e: Exception =>
        logger.error(s"Couldn't get a value $key from etcd", e)
        defaultValue
    }
  }

  def shutdown(): Unit = {
    etcdClient.shutdown()
  }
}
