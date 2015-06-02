package wh.application

import net.codingwell.scalaguice.ScalaModule
import wh.application.commodity.CommodityService

class ApplicationModule extends ScalaModule {
  override def configure(): Unit = {
    bind[CommodityService]
  }
}
