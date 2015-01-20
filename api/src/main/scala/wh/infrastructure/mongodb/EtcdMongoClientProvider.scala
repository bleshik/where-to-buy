package wh.infrastructure.mongodb

import wh.infrastructure.etcd.SyncEtcdClient
import com.mongodb.{MongoClient, MongoCredential, ServerAddress}
import net.nikore.etcd.EtcdClient

import scala.collection.JavaConverters._

class EtcdMongoClientProvider(val app: String) extends MongoClientProvider {
  private val NODES_KEY = "/mongo/replica/nodes"
  private val LOGIN_KEY = "/mongo/replica/" + app + "/login"
  private val DB_KEY = "/mongo/replica/" + app + "/db"
  private val PASSWORD_KEY = "/mongo/replica/" + app + "/pwd"
  private val LOCALHOST = List(new ServerAddress())
  private val etcdClient = new SyncEtcdClient(new EtcdClient(Option(System.getenv("ETCD_ENDPOINT")).getOrElse("http://172.17.42.1:4001")))

  var mongoClient: MongoClient = new MongoClient(addresses.asJava, List(credential).asJava)
  etcdClient.shutdown()
  override def get: MongoClient = mongoClient

  private def addresses: List[ServerAddress] = {
    etcdClient.list(NODES_KEY, List("127.0.0.1")).map(n =>
      new ServerAddress(
        n.substring(n.lastIndexOf('/') + 1),
        Integer.parseInt(etcdClient.get(n + "/port", "27017"))
      )
    )
  }
  private def credential: MongoCredential = MongoCredential.createMongoCRCredential(login, db, password)
  private def db: String = etcdClient.get(DB_KEY, app)
  private def login: String = etcdClient.get(LOGIN_KEY, app)
  private def password: Array[Char] = etcdClient.get(PASSWORD_KEY, app).toCharArray
}
