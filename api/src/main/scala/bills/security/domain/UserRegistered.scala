package bills.security.domain

import eventstore.api.Event

case class UserRegistered(id: Long, email: String, passwordHash: String) extends Event
