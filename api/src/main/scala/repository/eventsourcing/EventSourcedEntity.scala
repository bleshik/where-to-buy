package repository.eventsourcing

import java.lang.reflect.{InvocationTargetException, Method}

import eventstore.api.{InitialEvent, Event}

abstract class EventSourcedEntity[T <: EventSourcedEntity[T]](val initialEvent: InitialEvent[T]) extends Cloneable {
  private val MUTATE_METHOD_NAME = "when"
  private var mutatingChanges: List[Event] = List()
  private var version: Long = 1
  private var committedVersion: Long = 0

  def apply(event: Event): T = {
    val mutatedEntity = mutate(event)
    mutatedEntity.mutatingChanges = this.mutatingChanges :+ event
    mutatedEntity.version = this.version + 1
    mutatedEntity.committedVersion = this.committedVersion
    mutatedEntity
  }

  private def mutate(event: Event): T = {
    if (event == null) {
      return this.asInstanceOf[T]
    }
    val when = findMutatingMethod(event.getClass)
    if (when == null) {
      return this.asInstanceOf[T]
    }
    try {
      when.invoke(this, event).asInstanceOf[T]
    } catch {
      case e: InvocationTargetException => throw e.getCause
    }
  }

  def mutatedVersion: Long = {
    version
  }

  def unmutatedVersion: Long = {
    version - changes.length
  }

  def changes: List[Event] = {
    if ((version - mutatingChanges.length == 1) && committedVersion == 0) {
      initialEvent :: mutatingChanges
    } else {
      mutatingChanges
    }
  }

  def commitChanges(): T = {
    val entity: T = this.clone().asInstanceOf[T]
    entity.mutatingChanges = List()
    entity.version = this.version
    entity.committedVersion = this.version
    entity
  }

  private def findMutatingMethod(eventClass: Class[_]): Method = {
    //TODO: cache it
    try {
      this.getClass.getMethod(MUTATE_METHOD_NAME, eventClass)
    } catch {
      case e: NoSuchMethodException => null
    }
  }
}
