package wh.port.adapter.persistence

import com.mongodb.DB
import repository.eventsourcing.mongodb.MongoDbEventSourcedRepository
import wh.images.domain.model.{Image, ImageRepository}

class MongoDbImageRepository(override val db: DB)
  extends MongoDbEventSourcedRepository[Image, String](db) with ImageRepository
