package bills.infrastructure.mongodb

import com.mongodb.MongoClient

trait MongoClientProvider {
  def get: MongoClient
}
