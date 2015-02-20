package wh.application.akka

import javax.inject.{Inject, Provider}

import akka.actor._
import com.google.inject.Injector
import com.typesafe.config._
import net.codingwell.scalaguice.ScalaModule
import wh.application.extractor.EntryExtractingActor
import wh.application.rest.RestActor
import wh.infrastructure.Environment

import scala.reflect.ClassTag

class AkkaModule extends ScalaModule {
  def configure: Unit = {
    val system = ActorSystem("WhereToBuySystem", ConfigFactory.load("api", ConfigParseOptions.defaults(), ConfigResolveOptions.defaults.setAllowUnresolved(true))
      .withValue("hostname", ConfigValueFactory.fromAnyRef(Environment.balancerIp.orElse(Environment.privateIp).getOrElse("127.0.0.1")))
      .resolve())
    bind[ActorSystem].toInstance(system)
    bind[ActorRefFactory].toInstance(system)
    actor[EntryExtractingActor]
    actor[RestActor]
  }

  private def actor[T <: Actor: Manifest]: Unit = {
    bind[T]
    bind[ActorRef].annotatedWithName(manifest.runtimeClass.getSimpleName).toProvider(new ActorProvider[T]).asEagerSingleton()
  }
}

class ActorProvider[T <: Actor: ClassTag] extends Provider[ActorRef] {
  @Inject()
  var injector: Injector = null

  @Inject()
  var system: ActorSystem = null

  def get = {
    val actorClass = implicitly[ClassTag[T]].runtimeClass.asInstanceOf[Class[T]]
    system.actorOf(Props[T]({injector.getInstance(actorClass)}), actorClass.getSimpleName)
  }
}