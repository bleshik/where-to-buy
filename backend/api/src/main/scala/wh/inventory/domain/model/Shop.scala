package wh.inventory.domain.model

case class Shop(name: String, city: String)

object Shop {
  val Komus = Shop("Komus", "Москва")
  val Utkonos = Shop("Утконос", "Москва")
  val Cont = Shop("Седьмой Континент", "Москва")
}
