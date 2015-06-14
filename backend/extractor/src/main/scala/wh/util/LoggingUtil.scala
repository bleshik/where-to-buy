package wh.util

import com.typesafe.scalalogging.LazyLogging

object LoggingUtil extends LazyLogging {
  def logMemory(module: String): Unit = {
    new Thread() {
      override def run(): Unit = {
        while(true) {
          def total = Runtime.getRuntime.totalMemory()
          def active = total - Runtime.getRuntime.freeMemory()
          logger.info(s"Mem.$module.total=$total,Mem.$module.active=$active")
          Thread.sleep(1000)
        }
      }
    }.run()
  }
}
