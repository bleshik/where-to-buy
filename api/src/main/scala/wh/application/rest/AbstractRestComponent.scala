package wh.application.rest

import java.nio.charset.StandardCharsets

import akka.actor.ActorRefFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import repository.eventsourcing.EventSourcedEntity
import shapeless.HNil
import spray.http.HttpHeaders.Accept
import spray.http._
import spray.http.MediaTypes._
import spray.httpx.marshalling.Marshaller
import spray.routing.{Route, MalformedHeaderRejection, Directive0}

import scala.concurrent.ExecutionContext

abstract class AbstractRestComponent(val actorRefFactory: ActorRefFactory) extends RestComponent {

  private val objectMapper = (new ObjectMapper() with ScalaObjectMapper).registerModule(DefaultScalaModule)
  implicit val ec: ExecutionContext = actorRefFactory.dispatcher

  private var marshallers: Map[MediaType, (AnyRef) => HttpEntity] = Map()
  implicit var theMarshaller: Marshaller[AnyRef] = null
  protected def marshaller(mediaType: MediaType, marshaller: (AnyRef) => HttpEntity): Unit = {
    marshallers += (mediaType -> marshaller)
    theMarshaller = Marshaller.of[AnyRef](marshallers.map(m => ContentType(m._1)).toList: _*) { (value, contentType, ctx) ⇒
      ctx.marshalTo(marshallers(contentType.mediaType)(value))
    }
  }

  marshaller(`application/json`, (value: AnyRef) => { HttpEntity(`application/json`, HttpData(objectMapper.writeValueAsString(value), HttpCharsets.`UTF-8`)) })

  protected def doGetRoute: Route

  override def route: Route = acceptHeaderAccordingToExtension { doGetRoute }

  def acceptHeaderAccordingToExtension: Directive0 =
    mapRequestContext({ c =>
      val path = c.unmatchedPath.toString()
      val dot = path.lastIndexOf('.')
      if (dot < 0 || dot >= path.size) {
        c
      } else {
        val ext = path.substring(dot + 1)
        MediaTypes.forExtension(ext).map { t =>
          c.withUnmatchedPathMapped(p => Uri.Path(p.toString().substring(0, dot)))
           .withRequestMapped(r => r.copy(headers = r.headers.filterNot(_.is("accept")) :+ HttpHeaders.Accept(t)))
        }.getOrElse(c)
      }
    })
}
