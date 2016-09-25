package wh.port.adapter.persistence

import java.lang.reflect.ParameterizedType
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.{JsonReader, JsonToken, JsonWriter}
import com.google.gson.{Gson, TypeAdapter, TypeAdapterFactory}
import com.google.gson.GsonBuilder
import eventstore.util.dynamodb.DynamoDbObjectMapper
import eventstore.util.dynamodb.GsonDynamoDbObjectMapper
import cz.augi.gsonscala._

case class ScalaGsonDynamoDbObjectMapper() extends GsonDynamoDbObjectMapper(
  new GsonBuilder().registerBasicConverters()
    .registerTypeAdapterFactory(ScalaCollectionTypeAdapterFactory)
    .create()) {}

object ScalaCollectionTypeAdapterFactory extends TypeAdapterFactory {
  override def create[T](gson: Gson, t: TypeToken[T]): TypeAdapter[T] = {
    if (classOf[Set[_]].isAssignableFrom(t.getRawType))
      return new SetTypeAdapter(gson, t.asInstanceOf[TypeToken[Set[_]]]).asInstanceOf[TypeAdapter[T]]
    if (classOf[List[_]].isAssignableFrom(t.getRawType))
      return new ListTypeAdapter(gson, t.asInstanceOf[TypeToken[List[_]]]).asInstanceOf[TypeAdapter[T]]
    null
  }
}

abstract class ScalaCollectionTypeAdapter[T](gson: Gson, t: TypeToken[T]) extends TypeAdapter[T] {
  val innerType = t.getType.asInstanceOf[ParameterizedType].getActualTypeArguments()(0)

  override def write(out: JsonWriter, value: T): Unit = {
    out.beginArray()
    value.asInstanceOf[Iterable[_]].foreach(i => gson.toJson(i, innerType, out))
    out.endArray()
  }

  override def read(in: JsonReader): T = {
    var result = List()
    in.beginArray()
    while(in.peek() != JsonToken.END_ARRAY) {
       result = result :+ gson.fromJson(in, innerType)
    }
    in.endArray()
    fromList(result)
  }

  protected def fromList(list: List[_]): T
}

class SetTypeAdapter(gson: Gson, t: TypeToken[Set[_]]) extends ScalaCollectionTypeAdapter[Set[_]](gson, t) {
  override protected def fromList(list: List[_]): Set[_] = list.toSet
}

class ListTypeAdapter(gson: Gson, t: TypeToken[List[_]]) extends ScalaCollectionTypeAdapter[List[_]](gson, t) {
  override protected def fromList(list: List[_]): List[_] = list
}

