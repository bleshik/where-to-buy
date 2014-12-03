package bills.banking.domain.account

import bills.banking.domain.account.AccountType.AccountType
import eventstore.api.Event

case class AccountCreated(id: Long, accountType: AccountType, bank: String) extends Event
