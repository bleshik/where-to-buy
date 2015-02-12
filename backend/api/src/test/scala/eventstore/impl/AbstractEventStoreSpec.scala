package eventstore.impl

import java.util.ConcurrentModificationException
import java.util.concurrent.{TimeUnit, Executors, ExecutorService}

import eventstore.api.{Event, EventStore}
import org.scalatest.{Matchers, FlatSpec}

class AbstractEventStoreSpec extends FlatSpec with Matchers {
  protected def stressTest(eventStore: EventStore, concurrencyLevel: Int = 10, eventsPerThread: Int = 10): Unit = {
    val pool: ExecutorService = Executors.newFixedThreadPool(concurrencyLevel)
    var exceptionAmount = 0
    for (i <- 1 to concurrencyLevel) {
      pool.submit(new Runnable {
        override def run(): Unit = {
          val streamName = "stream"
          for (_ <- 1 to eventsPerThread) {
            val events = List(DummyEvent())
            var success = false
            while(!success) {
              val eventStream = eventStore.stream(streamName)
              try {
                eventStore.append(streamName, eventStream.version, events)
                success = true
              } catch {
                case e: ConcurrentModificationException => exceptionAmount += 1
              }
            }
          }
        }
      })
    }
    pool.shutdown()
    try {
      if (pool.awaitTermination(10000, TimeUnit.MILLISECONDS)) {
        eventStore.stream("stream").version should be(concurrencyLevel * eventsPerThread)
      } else {
        fail()
      }
    } finally {
      System.out.println("ConcurrentModificationException occurred " + exceptionAmount + " times")
    }
  }
}
case class DummyEvent() extends Event
