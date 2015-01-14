package bills.security.domain

import repository.eventsourcing.IdentifiedEventSourcedEntity

case class User(id: Long, email: String, passwordHash: String) extends IdentifiedEventSourcedEntity[User, Long] {
  if (!email.contains("@")) {
    new IllegalArgumentException(email + " is not a valid email")
  }

  apply(new UserRegistered(id, email, passwordHash))

  protected def this(e: UserRegistered) = this(e.id, e.email, e.passwordHash)
}
