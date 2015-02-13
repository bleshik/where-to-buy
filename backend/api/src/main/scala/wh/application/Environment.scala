package wh.application

import com.google.inject.Stage

object Environment extends Enumeration {
  type Environment = Value
  val DEV, PROD = Value

  def current = if ("production".equals(System.getenv("ENVIRONMENT"))) PROD else DEV

  def stage = if (current == PROD) Stage.PRODUCTION else Stage.DEVELOPMENT
}
