package bills.banking.domain.account

import bills.banking.domain.account.AccountType.AccountType
import repository.IdentifiedEntity
import repository.eventsourcing.EventSourcedEntity

case class Account(id: Long, accountType: AccountType, bank: String) extends EventSourcedEntity[Account] with IdentifiedEntity[Long] {
  if (accountType == AccountType.Cash && bank != null) {
    throw new IllegalArgumentException("Cash account cannot have a bank")
  }
  apply(AccountCreated(id, accountType, bank))

  def this(id: Long) {
    this(id, AccountType.Cash, null)
  }

  protected def this(event: AccountCreated) {
    this(event.id, event.accountType, event.bank)
  }
}
