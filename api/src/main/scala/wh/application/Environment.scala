package wh.application

object Environment extends Enumeration {
  type Environment = Value
  val DEV, PROD = Value

  def current = if (System.getProperty("env", "DEV") == "DEV") DEV else PROD
}
