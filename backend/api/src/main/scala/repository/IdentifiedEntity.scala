package repository

trait IdentifiedEntity[K] {
  def id: K

  override def hashCode(): Int = {
    id.hashCode()
  }

  override def equals(obj: scala.Any): Boolean = {
    if (obj == null) {
      return false
    }
    if (obj.getClass != getClass) {
      return false
    }
    this.id.equals(obj.asInstanceOf[IdentifiedEntity[_]].id)
  }
}
