package wh.infrastructure.mongodb

import com.typesafe.scalalogging.LazyLogging
import wh.infrastructure.Environment
import wh.infrastructure.etcd.SyncEtcdClient
import com.mongodb.{MongoClient, MongoCredential, ServerAddress}

import scala.collection.JavaConverters._

class EtcdMongoClientProvider(val app: String) extends MongoClientProvider with LazyLogging {
  private val NODES_KEY = "/mongo/replica/nodes"
  private val LOGIN_KEY = "/mongo/replica/" + app + "/login"
  private val DB_KEY = "/mongo/replica/" + app + "/db"
  private val PASSWORD_KEY = "/mongo/replica/" + app + "/pwd"
  private val DEFAULT = List(new ServerAddress(Environment.privateIp.getOrElse("127.0.0.1"), 27017))
  private val etcdClient = new SyncEtcdClient(Environment.etcdEndpoint)

  var mongoClient: MongoClient = new MongoClient(addresses.asJava, List(credential).asJava)
  override def get: MongoClient = mongoClient

  private def addresses: List[ServerAddress] = {
    val result = etcdClient.list(NODES_KEY).map(o =>
      o.map {n =>
        new ServerAddress(
          n.substring(n.lastIndexOf('/') + 1),
          Integer.parseInt(etcdClient.get(n + "/port").getOrElse("27017"))
        )
      }
    ).getOrElse(DEFAULT)
    logger.debug(s"Using mongodb addresses: $result")
    result
  }
  private def credential: MongoCredential = MongoCredential.createMongoCRCredential(login, db, password)
  private def db: String = {
    val result = etcdClient.get(DB_KEY).getOrElse(app)
    logger.debug(s"Using mongodb db name: $result")
    result
  }
  private def login: String = {
    val result = etcdClient.get(LOGIN_KEY).getOrElse(app)
    logger.debug(s"Using mongodb login: $result")
    result
  }
  private def password: Array[Char] = etcdClient.get(PASSWORD_KEY).get.toCharArray
}
