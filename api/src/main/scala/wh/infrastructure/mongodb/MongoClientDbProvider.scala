package wh.infrastructure.mongodb

import com.mongodb.DB

class MongoClientDbProvider(val mongoClientProvider: MongoClientProvider, val dbName: String) extends MongoDbProvider {
  override def get: DB = {
    mongoClientProvider.get.getDB(dbName)
  }
}
