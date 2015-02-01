package wh.application.akka

import javax.inject.{Inject, Provider}

import akka.actor._
import com.google.inject.Injector
import com.typesafe.config.ConfigFactory
import net.codingwell.scalaguice.ScalaModule
import wh.application.extractor.EntryExtractingActor
import wh.rest.MyServiceActor

import scala.reflect.ClassTag

class AkkaModule extends ScalaModule
{
  def configure: Unit = {
    val system = ActorSystem("WhereToBuySystem", ConfigFactory.load("api"))
    bind[ActorSystem].toInstance(system)
    actor[MyServiceActor]
    actor[EntryExtractingActor]
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