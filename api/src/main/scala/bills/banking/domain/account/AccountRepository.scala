package bills.banking.domain.account

import repository.PersistenceOrientedRepository

trait AccountRepository extends PersistenceOrientedRepository[Account, Long]  {

}
