package bills.security.domain

import repository.PersistenceOrientedRepository

trait UserRepository extends PersistenceOrientedRepository[User, Long] {

}
