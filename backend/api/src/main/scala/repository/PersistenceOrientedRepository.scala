package repository

trait PersistenceOrientedRepository[T, K] {
  def get(id: K): Option[T]
  def contains(id: K): Boolean = get(id).isDefined
  def save(entity: T): T
  def size: Long
  def all: Set[T]
}