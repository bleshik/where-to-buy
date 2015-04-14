package wh.util

import com.typesafe.scalalogging.LazyLogging

import scala.util.{Failure, Success, Try}

trait LoggingHandling extends LazyLogging {
  def handle[T](t: Try[T]): Option[T] = {
    t match {
      case Success(i) => Some(i)
      case Failure(thrown) => thrown match {
        case exception: Exception =>
          logger.error("The try tailed", thrown)
          None
        case other: Throwable =>
          throw other
      }
    }
  }

}
