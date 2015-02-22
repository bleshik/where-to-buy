package repository.eventsourcing.mongodb

import com.mongodb.MongoTimeoutException

abstract class MongoDbMigrator {
  def migrate(): Unit = {
    try {
      doMigrate()
    } catch {
      case e:MongoTimeoutException => migrate()
    }
  }

  protected def doMigrate(): Unit
}

object Migration {
  def apply(migration: => Unit): Unit = {
    new MongoDbMigrator {
      override protected def doMigrate(): Unit = migration
    }.migrate()
  }
}