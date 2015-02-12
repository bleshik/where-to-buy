package eventstore.impl

import com.github.fakemongo.Fongo
import com.mongodb.DB

trait MongoDbSpec {
  def db(dbName: String = "WhereToBuy"): DB = {
    new Fongo(getClass.getCanonicalName).getDB(dbName)
  }
}
