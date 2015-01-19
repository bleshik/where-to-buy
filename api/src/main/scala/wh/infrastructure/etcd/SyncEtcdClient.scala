package wh.infrastructure.etcd

import net.nikore.etcd.EtcdClient

import scala.concurrent.Await
import scala.concurrent.duration.Duration._

class SyncEtcdClient(val etcdClient: EtcdClient) {

  def list(dir: String, defaultValues: List[String]): List[String] = {
    try {
      Await.result(etcdClient.listDir(dir), Inf).node.nodes.map(o => o.map(e => e.key)).getOrElse(defaultValues)
    } catch {
      case e: Exception => defaultValues
    }
  }

  def get(key: String, defaultValue: String) : String = {
    try {
      Await.result(etcdClient.getKey(key), Inf).node.value.getOrElse(defaultValue)
    } catch {
      case e: Exception => defaultValue
    }
  }
}
