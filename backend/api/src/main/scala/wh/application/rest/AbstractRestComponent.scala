package wh.application.rest

import akka.actor.ActorRefFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import shapeless.HNil
import spray.http.CacheDirectives._
import spray.http.HttpHeaders.{Accept, `Cache-Control`}
import spray.http.MediaTypes._
import spray.http._
import spray.httpx.marshalling.Marshaller
import spray.routing.{Directive0, MalformedHeaderRejection, Route}

import scala.concurrent.ExecutionContext

abstract class AbstractRestComponent(val actorRefFactory: ActorRefFactory) extends RestComponent {

  private val objectMapper = (new ObjectMapper() with ScalaObjectMapper).registerModule(DefaultScalaModule)

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

  def cache(sec: Long): Directive0 =
    respondWithHeader(`Cache-Control`(`public`, `max-age`(sec)))

  def cacheForDay: Directive0 =
    cache(24L * 60L * 60L)

  def cacheImagesForDay: Directive0 =
    accept(`image/jpeg`) & cacheForDay

  def accept(mr: MediaRange*): Directive0 =
    extract(_.request.headers).flatMap[HNil] {
      case headers if headers.contains(Accept(mr)) ⇒ pass
      case _                                       ⇒ reject(MalformedHeaderRejection("Accept", s"Only the following media types are supported: ${mr.mkString(", ")}"))
    } & cancelAllRejections(ofType[MalformedHeaderRejection])

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
