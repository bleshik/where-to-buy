package wh.application.rest

import akka.actor.ActorRefFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import spray.http.HttpEntity
import spray.http.MediaTypes._
import spray.httpx.marshalling.Marshaller

import scala.concurrent.ExecutionContext

abstract class AbstractRestComponent(val actorRefFactory: ActorRefFactory) extends RestComponent {

  private val objectMapper = (new ObjectMapper() with ScalaObjectMapper).registerModule(DefaultScalaModule)

  implicit val ec: ExecutionContext = actorRefFactory.dispatcher
  implicit val accountMarshaller = Marshaller.of[AnyRef](`application/json`) { (value, contentType, ctx) ⇒
    ctx.marshalTo(HttpEntity(contentType, objectMapper.writeValueAsString(value)))
  }
}
