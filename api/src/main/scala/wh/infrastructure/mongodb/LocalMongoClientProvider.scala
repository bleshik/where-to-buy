package wh.infrastructure.mongodb

import com.mongodb.MongoClient

class LocalMongoClientProvider extends MongoClientProvider {
  override def get: MongoClient = new MongoClient()
}
