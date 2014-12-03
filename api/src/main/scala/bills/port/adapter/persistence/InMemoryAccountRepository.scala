package bills.port.adapter.persistence

import bills.banking.domain.account.{Account, AccountRepository}
import eventstore.impl.InMemoryEventStore
import repository.eventsourcing.EventSourcedRepository

class InMemoryAccountRepository extends EventSourcedRepository[Account, Long](new InMemoryEventStore) with AccountRepository {
  this.save(new Account(0))
  this.save(new Account(1))
  this.save(new Account(2))
}
