package actor.domain.model

import java.net.URL
import actor.port.adapter.local.LocalEventTransport
import org.scalatest.{FlatSpec, Matchers}

class ActorSpec extends FlatSpec with Matchers {
  it should "handle event correctly" in {
    new Dispatcher(new LocalEventTransport()).send(classOf[DummyActor], "Test")
    var i: Int = 0
    while(!DummyActorResult.result.equals(42L) && i <= 1000) {
      Thread.sleep(100)
      i = i + 100
    }
    if (i > 1000) {
      fail()
    }
  }
}

class DummyActor extends Actor {
  protected def when(event: String): Unit = {
    sendToMyself(42)
  }

  protected def when(event: java.lang.Integer): Unit = {
    handle(classOf[DummyActor], 42L)
  }

  protected def when(event: java.lang.Long): Unit = {
    DummyActorResult.result = event
  }
}

object DummyActorResult {
  var result: Any = 123L
}
