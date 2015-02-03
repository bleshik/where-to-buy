package wh.application.rest

import net.codingwell.scalaguice.{ScalaModule, ScalaMultibinder}
import wh.application.rest.resource.CommoditiesResource

class RestModule extends ScalaModule {
  val resourceClasses = List(
    classOf[CommoditiesResource]
  )

  override def configure(): Unit = {
    val resources = ScalaMultibinder.newSetBinder[RestComponent](binder)
    resourceClasses.foreach(resources.addBinding.to(_))
  }
}
