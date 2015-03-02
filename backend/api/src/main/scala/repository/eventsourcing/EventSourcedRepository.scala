package repository.eventsourcing

import java.lang.Math.min
import java.lang.reflect.{Constructor, ParameterizedType}
import java.util.ConcurrentModificationException

import com.typesafe.scalalogging.LazyLogging
import eventstore.api.{Event, EventStore, InitialEvent}
import repository.{IdentifiedEntity, PersistenceOrientedRepository}

abstract class EventSourcedRepository[T <: EventSourcedEntity[T] with IdentifiedEntity[K], K](val eventStore: EventStore)
  extends PersistenceOrientedRepository[T, K] with LazyLogging {
  override def get(id: K): Option[T] = {
    get(id, -1)
  }

  private def get(id: K, version: Long): Option[T] = {
    getByStreamName(streamName(id), version, snapshot(id, version))
  }

  private def getByStreamName(streamName: String, version: Long, snapshot: Option[T] = None): Option[T] = {
    val stream = eventStore.streamSince(streamName, snapshot.map(e => e.unmutatedVersion).getOrElse(-1))
    if (stream.events.isEmpty) {
      return snapshot
    }
    var entity = snapshot.getOrElse(init(stream.events.head))
    var eventsRemaining = if (snapshot.isEmpty)stream.events.tail else stream.events
    while(entity.unmutatedVersion != version && eventsRemaining.nonEmpty) {
      entity = entity.apply(eventsRemaining.head)
      eventsRemaining = eventsRemaining.tail
    }
    Some(entity.commitChanges())
  }

  protected def saveSnapshot(entity: T): Unit = {}

  protected def snapshot(id: K, before: Long): Option[T] = { None }

  private def init(initEvent: Event): T = {
    initEvent.asInstanceOf[InitialEvent[T]].initializedObject()
  }

  private def getAndApply(id: K, after: Long, changes: List[Event]): Option[T] = {
    if (after <= 0) {
      getAndApply(id, 1, changes.tail)
    } else {
      get(id, after) match {
        case Some(entity) =>
          var mutatedEntity = entity.asInstanceOf[T]
          val stream = eventStore.streamSince(streamName(id), after)
          var i: Int = 0
          while (min(changes.length, stream.events.length) > i && changes(i).equals(stream.events(i))) {
            mutatedEntity = mutatedEntity.apply(stream.events(i))
            i += 1
          }
          mutatedEntity = mutatedEntity.commitChanges()
          changes.takeRight(changes.length - i).foreach { e => mutatedEntity = mutatedEntity.apply(e) }
          Some(mutatedEntity)
        case None => None
      }
    }
  }

  override def save(entity: T): T = {
    if (entity.changes.isEmpty) {
      entity
    } else {
      try {
        eventStore.append(streamName(entity.id), entity.unmutatedVersion, entity.changes)
      } catch {
        case e: ConcurrentModificationException =>
          save(getAndApply(entity.id, entity.unmutatedVersion, entity.changes).get)
      }
      try {
        saveSnapshot(entity)
      } catch {
        case e: Exception => logger.error("Couldn't save snapshot", e)
      }
      entity
    }
  }

  override def size: Long = {
    eventStore.size
  }

  override def all : Set[T] = {
    eventStore.streamNames.flatMap(s => getByStreamName(s, -1))
  }

  protected def entityClass: Class[T] = {
    this.getClass
      .getGenericSuperclass
      .asInstanceOf[ParameterizedType]
      .getActualTypeArguments()(0)
      .asInstanceOf[Class[T]]
  }

  private def constructor(eventClass: Class[_ <: Event]): Constructor[T] = {
    val c =  entityClass
      .getConstructor(eventClass)
      .asInstanceOf[Constructor[T]]
    c.setAccessible(true)
    c
  }

  private def streamName(id: K): String = {
     streamPrefix + id
  }

  private def streamPrefix: String = {
    this.entityClass.getSimpleName
  }
}
