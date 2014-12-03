package bills.banking.domain.account

object AccountType extends Enumeration {
  type AccountType = Value
  val Savings, CreditCard, Cash = Value
}
