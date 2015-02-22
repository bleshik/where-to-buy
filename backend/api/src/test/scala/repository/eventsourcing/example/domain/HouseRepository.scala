package repository.eventsourcing.example.domain

import repository.PersistenceOrientedRepository

trait HouseRepository extends PersistenceOrientedRepository[House, String] {

}
