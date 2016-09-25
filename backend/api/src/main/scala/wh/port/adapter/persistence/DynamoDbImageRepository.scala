package wh.port.adapter.persistence

import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.document.Item
import ddd.repository.dynamodb.DynamoDbRepository
import eventstore.util.dynamodb.DynamoDbObjectMapper
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO
import scala.collection.JavaConverters._
import wh.images.domain.model.{InMemoryImage, Image, ImageRepository}
import wh.infrastructure.aws.S3Util

class DynamoDbImageRepository(val client: AmazonDynamoDB)
  extends DynamoDbRepository[Image, String](client, ScalaGsonDynamoDbObjectMapper()) with ImageRepository {

  protected override def serialize(entity: Image): Item = super.serialize(entity).removeAttribute("data")

  override def save(entity: Image): Image = {
    if (entity.isInstanceOf[InMemoryImage]) {
      val data = entity.asInstanceOf[InMemoryImage].data
      val extension = ImageIO.getImageReaders(
        ImageIO.createImageInputStream(new ByteArrayInputStream(data))
      ).asScala.find(_ => true).map((e) => s".${e.getFormatName()}").getOrElse("")
      super.save(
        entity.asInstanceOf[InMemoryImage].uploadedToS3(
          S3Util.uploadPublicFile("wh-prod", s"images/${entity.name}${extension}", data).toString()
        )
      );
    } else {
      super.save(entity)
    }
  }
  
}
