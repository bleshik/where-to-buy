package repository.eventsourcing

import repository.PersistenceOrientedRepository

trait TemporalPersistenceOrientedRepository[T, K] extends PersistenceOrientedRepository[T, K] {
  def contained(id: K): Boolean
}
