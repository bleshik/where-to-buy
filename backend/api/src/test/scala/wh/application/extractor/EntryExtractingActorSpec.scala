package wh.application.extractor

import org.scalatest.{Matchers, FlatSpec}

class EntryExtractingActorSpec extends FlatSpec with Matchers {
  it should "not filter valid commodities" in {
    Set(
      List((0L, 100L)),
      List((0L, 100L), (1L, 200L)),
      List((0L, 100L), (1L, 200L), (2L, 300L), (3L, 220L)),
      List((0L, 100L), (1L, 200L), (2L, 300L), (3L, 220L), (4L, 200L)),
      List((0L, 100L), (1L, 200L), (2L, 300L), (3L, 220L), (4L, 200L), (5L, 300L))
    ).forall(!EntryExtractingActor.weird(_)) should be (right = true)
  }

  it should "detect invalid commodities" in {
    Set(
      List((0L, 100L), (1L, 200L), (2L, 100L), (3L, 200L)),
      List((0L, 100L), (1L, 200L), (2L, 300L), (3L, 100L), (4L, 200L), (5L, 300L)),
      List((0L, 100L), (1L, 200L), (2L, 300L), (3L, 400L), (4L, 100L), (5L, 200L), (6L, 300L), (7L, 400L))
    ).forall(EntryExtractingActor.weird) should be (right = true)
  }
}
