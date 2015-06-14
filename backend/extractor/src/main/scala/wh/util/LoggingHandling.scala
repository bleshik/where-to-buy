package wh.util

import com.typesafe.scalalogging.LazyLogging

import scala.util.{Failure, Success, Try}

trait LoggingHandling extends LazyLogging {
  def handle[T](t: Try[T], debug: Set[Class[_ <: Exception]] = Set()): Option[T] = {
    t match {
      case Success(i) => Some(i)
      case Failure(thrown) => thrown match {
        case exception: Exception =>
          if (debug.contains(exception.getClass)) {
            logger.debug("The try tailed", thrown)
          } else {
            logger.error("The try tailed", thrown)
          }
          None
        case other: Throwable =>
          throw other
      }
    }
  }
}
