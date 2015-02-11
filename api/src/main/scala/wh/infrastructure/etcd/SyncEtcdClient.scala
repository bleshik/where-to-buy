package wh.infrastructure.etcd

import net.nikore.etcd.EtcdClient

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class SyncEtcdClient(val etcdClient: EtcdClient, val duration: Duration = Duration(1, "s")) {

  def list(dir: String, defaultValues: List[String]): List[String] = {
    try {
      Await.result(etcdClient.listDir(dir), duration).node.nodes.map(o => o.map(e => e.key)).getOrElse(defaultValues)
    } catch {
      case e: Exception => defaultValues
    }
  }

  def get(key: String, defaultValue: String) : String = {
    try {
      Await.result(etcdClient.getKey(key), duration).node.value.getOrElse(defaultValue)
    } catch {
      case e: Exception => defaultValue
    }
  }

  def shutdown(): Unit = {
    etcdClient.shutdown()
  }
}
