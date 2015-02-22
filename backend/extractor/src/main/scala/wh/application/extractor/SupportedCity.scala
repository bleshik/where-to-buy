package wh.application.extractor

object SupportedCity extends Enumeration {
  case class City(name: String) extends super.Val
  implicit def valueToCity(x: Value): City = x.asInstanceOf[City]
  val Moscow = City("Москва")
}
