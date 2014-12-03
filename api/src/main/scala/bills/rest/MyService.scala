package bills.rest

import akka.actor.Actor
import bills.port.adapter.persistence.InMemoryAccountRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import spray.http.ContentTypes._
import spray.http.HttpEntity
import spray.httpx.marshalling.Marshaller
import spray.routing._

import scala.concurrent.ExecutionContext

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class MyServiceActor extends Actor with MyService {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(myRoute)
}


// this trait defines our service behavior independently from the service actor
trait MyService extends HttpService {
  implicit val ec: ExecutionContext = actorRefFactory.dispatcher
  val accountRepository = new InMemoryAccountRepository
  val objectMapper = (new ObjectMapper() with ScalaObjectMapper).registerModule(DefaultScalaModule)
  implicit val accountMarshaller = Marshaller.of[AnyRef](`application/json`) { (value, contentType, ctx) ⇒
      ctx.marshalTo(HttpEntity(contentType, objectMapper.writeValueAsString(value)))
  }
  val myRoute =
    pathPrefix("accounts") {
      pathEnd {
        get {
          complete { accountRepository.all }
        }
      } ~
      path(IntNumber) { id =>
        get {
          complete { accountRepository.get(id) }
        }
      }
    }
}