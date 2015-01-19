package eventstore.impl

import com.mongodb.casbah.Imports._
import com.novus.salat._

class MongoDbSerializer[T <: AnyRef: Manifest] {
  implicit val ctx = new Context {
    val name = "When-Necessary-Context"

    override val typeHintStrategy = StringTypeHintStrategy(when = TypeHintFrequency.Always,
      typeHint = "_typeHint")

  }

  def deserialize(mongoObject: DBObject): T = {
    grater[T].asObject(mongoObject)
  }

  def serialize(obj: T): DBObject = {
    grater[T].asDBObject(obj)
  }
}
