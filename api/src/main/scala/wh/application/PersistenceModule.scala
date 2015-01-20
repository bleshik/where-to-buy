package wh.application

import javax.inject.Inject

import com.mongodb.DB
import net.codingwell.scalaguice.ScalaModule
import wh.domain.model.CommodityRepository
import wh.infrastructure.mongodb.{LocalMongoClientProvider, MongoClientProvider, EtcdMongoClientProvider, MongoClientDbProvider}
import wh.port.adapter.persistence.MongoDbCommodityRepository

class PersistenceModule extends ScalaModule {
  override def configure(): Unit = {
    if (Environment.current == Environment.DEV) {
      bind[MongoClientProvider].to[LocalMongoClientProvider]
    } else {
      bind[MongoClientProvider].toInstance(new EtcdMongoClientProvider("where"))
    }
  }

  @Inject
  def db(mongoClientProvider: MongoClientProvider): DB =
    new MongoClientDbProvider(mongoClientProvider, "where").get

  @Inject
  def commodityRepository(db: DB): CommodityRepository =
    new MongoDbCommodityRepository(db)
}
