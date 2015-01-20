package wh.application

import net.codingwell.scalaguice.ScalaModule
import wh.application.akka.AkkaModule
import wh.application.extractor.EntryExtractingActor
import wh.rest.MyServiceActor

class ApiModule extends ScalaModule {
  override def configure(): Unit = {
    install(new AkkaModule)
    install(new PersistenceModule)
    bind[MyServiceActor]
    bind[EntryExtractingActor]
  }
}
