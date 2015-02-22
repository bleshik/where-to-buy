package wh.infrastructure.mongodb

import com.mongodb.DB

trait MongoDbProvider {
  def get: DB
}
