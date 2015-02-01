package wh.application

import net.codingwell.scalaguice.ScalaModule
import wh.application.akka.AkkaModule

class ApiModule extends ScalaModule {
  override def configure(): Unit = {
    install(new AkkaModule)
    install(new PersistenceModule)
  }
}
