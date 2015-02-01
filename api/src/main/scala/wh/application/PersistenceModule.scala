package wh.application

import com.google.inject.Provides
import javax.inject.Singleton
import com.mongodb.DB
import net.codingwell.scalaguice.ScalaModule
import wh.domain.model.CommodityRepository
import wh.infrastructure.mongodb.{EtcdMongoClientProvider, LocalMongoClientProvider, MongoClientDbProvider, MongoClientProvider}
import wh.port.adapter.persistence.MongoDbCommodityRepository

class PersistenceModule extends ScalaModule {
  override def configure(): Unit = {
    if (Environment.current == Environment.DEV) {
      bind[MongoClientProvider].to[LocalMongoClientProvider]
    } else {
      bind[MongoClientProvider].toInstance(new EtcdMongoClientProvider("where"))
    }
  }

  @Provides @Singleton
  def db(mongoClientProvider: MongoClientProvider): DB =
    new MongoClientDbProvider(mongoClientProvider, "where").get

  @Provides @Singleton
  def commodityRepository(db: DB): CommodityRepository =
    new MongoDbCommodityRepository(db)
}
