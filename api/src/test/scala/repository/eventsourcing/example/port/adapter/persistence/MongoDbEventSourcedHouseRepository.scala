package repository.eventsourcing.example.port.adapter.persistence

import com.mongodb.DB
import repository.eventsourcing.example.domain.{HouseRepository, House}
import repository.eventsourcing.mongodb.MongoDbEventSourcedRepository

class MongoDbEventSourcedHouseRepository(override val db: DB)
  extends MongoDbEventSourcedRepository[House, String](db) with HouseRepository {

}
