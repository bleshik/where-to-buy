package wh.images.domain.model

import repository.PersistenceOrientedRepository

trait ImageRepository extends PersistenceOrientedRepository[Image, String] {

}
