package wh.application

import actor.port.adapter.aws.AwsUtil
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.google.inject.Provides
import eventstore.util.dynamodb.LocalAmazonDynamoDbClient
import javax.inject.Singleton
import net.codingwell.scalaguice.ScalaModule
import wh.images.domain.model.ImageRepository
import wh.infrastructure.Environment
import wh.inventory.domain.model.CommodityRepository
import wh.port.adapter.persistence.DynamoDbCommodityRepository
import wh.port.adapter.persistence.DynamoDbImageRepository

class PersistenceModule extends ScalaModule {
  override def configure(): Unit = {
    if (Environment.current == Environment.DEV) {
      bind[AmazonDynamoDBClient].to[LocalAmazonDynamoDbClient]
    } else {
      val client = new AmazonDynamoDBClient(new ClasspathPropertiesFileCredentialsProvider())
      client.setRegion(AwsUtil.REGION)
      bind[AmazonDynamoDBClient].toInstance(client)
    }
  }

  @Provides @Singleton
  def commodityRepository(db: AmazonDynamoDBClient): CommodityRepository =
    new DynamoDbCommodityRepository(db)

  @Provides @Singleton
  def imageRepository(db: AmazonDynamoDBClient): ImageRepository =
    new DynamoDbImageRepository(db)
}
