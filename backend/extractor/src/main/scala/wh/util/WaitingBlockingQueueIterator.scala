package wh.util

import java.util.concurrent.BlockingQueue
import java.util.concurrent.TimeUnit

class WaitingBlockingQueueIterator[T](val queue: BlockingQueue[T], val timeout: Long = 2L, unit: TimeUnit = TimeUnit.SECONDS) extends Iterator[T] {
  protected var nextItem: Option[T] = None
  protected var finished: Boolean = false

  def hasNext: Boolean = {
    if (nextItem.nonEmpty) { return true }
    if (finished) { return false }
    nextItem = Option(queue.poll(timeout, unit))
    finished = nextItem.isEmpty
    !finished
  }

  def next(): T = {
    if (!hasNext) {
      throw new NoSuchElementException()
    }
    try {
      return nextItem.get
    } finally {
      nextItem = None
    }
  }
}
