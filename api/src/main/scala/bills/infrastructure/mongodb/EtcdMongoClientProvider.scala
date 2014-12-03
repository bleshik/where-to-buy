package bills.infrastructure.mongodb

import com.mongodb.{MongoClient, ServerAddress}
import net.nikore.etcd.EtcdClient

import scala.collection.JavaConverters._
import scala.concurrent.Await
import scala.concurrent.duration.Duration.Inf

class EtcdMongoClientProvider extends MongoClientProvider {
  private val LOCALHOST = List(new ServerAddress())
  val etcdClient = new EtcdClient(Option(System.getenv("ETCD_ENDPOINT")).getOrElse("http://172.17.42.1:4001"))
  var mongoClient: MongoClient = new MongoClient(addresses.asJava)
  override def get: MongoClient = mongoClient

  private def addresses: List[ServerAddress] = {
    try {
      Await.result(etcdClient.listDir("/mongo/replica/nodes"), Inf).node.nodes.map(o => o.flatMap(e => {
        try {
          Some(new ServerAddress(e.key.substring(e.key.lastIndexOf('/') + 1),
            Integer.parseInt(Await.result(etcdClient.getKey(e.key + "/port"), Inf).node.value.getOrElse("27017"))))
        } catch {
          case e: Exception => None
        }
      })).map(hosts => if (hosts.nonEmpty) hosts else LOCALHOST)
         .getOrElse(LOCALHOST)
    } catch {
      case e: Exception => LOCALHOST
    }
  }
}
