package eventstore.api

import org.apache.commons.lang3.builder.{EqualsBuilder, HashCodeBuilder}

abstract class Event {
  private var _occurredOn = -1L

  def occurredOn: Long = _occurredOn

  override def hashCode(): Int = {
    HashCodeBuilder.reflectionHashCode(this, "_occurredOn")
  }

  override def equals(obj: scala.Any): Boolean = {
    EqualsBuilder.reflectionEquals(this, obj, "_occurredOn")
  }
}