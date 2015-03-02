package wh.util

import scala.collection.IterableLike
import scala.collection.parallel.{ParIterable, ForkJoinTaskSupport, ParIterableLike}
import scala.concurrent.forkjoin.ForkJoinPool

object ConcurrencyUtil {
  implicit def extendedPar[T, Repr <: ParIterable[T], Sequential <: Iterable[T] with IterableLike[T, Sequential]](par: ParIterableLike[T, Repr, Sequential]) = new {
    def withMinThreads(n: Int) = {
      par.tasksupport = new ForkJoinTaskSupport(new ForkJoinPool(Math.max(n, Runtime.getRuntime.availableProcessors())))
      par
    }
  }
}
