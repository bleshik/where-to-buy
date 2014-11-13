package eventstore.api

import org.apache.commons.lang3.builder.{EqualsBuilder, HashCodeBuilder}

trait Event {
  override def hashCode(): Int = {
    HashCodeBuilder.reflectionHashCode(this)
  }

  override def equals(obj: scala.Any): Boolean = {
    EqualsBuilder.reflectionEquals(this, obj)
  }
}