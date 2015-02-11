package wh.application

import net.codingwell.scalaguice.ScalaModule
import wh.application.akka.AkkaModule
import wh.application.rest.RestModule

class ApiModule extends ScalaModule {
  override def configure(): Unit = {
    install(new AkkaModule)
    install(new PersistenceModule)
    install(new RestModule)
  }
}
