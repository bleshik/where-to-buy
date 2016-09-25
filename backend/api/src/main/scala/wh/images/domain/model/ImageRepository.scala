package wh.images.domain.model

import ddd.repository.PersistenceOrientedRepository

trait ImageRepository extends PersistenceOrientedRepository[Image, String] {

}
