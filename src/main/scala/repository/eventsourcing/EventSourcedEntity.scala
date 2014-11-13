package repository.eventsourcing

import java.lang.reflect.{InvocationTargetException, Method}

import eventstore.api.Event

abstract class EventSourcedEntity[T <: EventSourcedEntity[T]] extends Cloneable {
  private val MUTATE_METHOD_NAME = "when"
  private var initEvent: Event = null
  private var mutatingChanges: List[Event] = List()
  private var version: Long = 0

  def apply(event: Event): T = {
    if (version == 0) {
      initEvent = event
    } else if (initEvent.getClass == event.getClass) {
      return this.asInstanceOf[T]
    }
    val mutatedEntity = mutate(event)
    mutatedEntity.mutatingChanges = this.mutatingChanges :+ event
    mutatedEntity.version = this.version + 1
    mutatedEntity.initEvent = this.initEvent
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
    version - mutatingChanges.length
  }

  def changes: List[Event] = {
    mutatingChanges
  }

  def commitChanges(): T = {
    val entity: T = this.clone().asInstanceOf[T]
    entity.mutatingChanges = List()
    entity.version = this.version
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
