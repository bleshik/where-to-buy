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
      List((0L, 100L), (1L, 200L), (2L, 300L), (3L, 400L), (4L, 100L), (5L, 200L), (6L, 300L), (7L, 400L)),
      List((0L, 63400L),(1L, 32100L),(2L, 63400L),(3L, 32100L),(4L, 63400L),(5L, 32100L),(6L, 63400L),(7L, 32100L),(8L, 63400L),(9L, 32100L),(10L, 63400L),(11L, 32100L),(12L, 63400L),(13L, 32100L),(14L, 63400L),(15L, 32100L),(16L, 63400L),(17L, 32100L),(18L, 63400L),(19L, 32100L),(20L, 63400L),(21L, 32100L),(22L, 63400L),(23L, 32100L),(24L, 63400L),(25L, 32100L),(26L, 63400L),(27L, 32100L)),
      List((0L, 17000L),(1L, 16900L),(2L, 17000L),(3L, 16900L),(4L, 17000L),(5L, 16900L),(6L, 18300L),(7L, 16900L),(8L, 18300L),(9L, 17000L),(10L, 16900L),(11L, 17000L),(12L, 18300L),(13L, 17000L),(14L, 16900L),(15L, 17000L),(16L, 16900L),(17L, 18300L),(18L, 16900L),(19L, 17000L),(20L, 16900L),(21L, 18300L),(22L, 16900L),(23L, 17000L),(24L, 16900L),(25L, 17000L),(26L, 16900L),(27L, 17000L),(28L, 18300L),(29L, 16900L),(30L, 17000L),(31L, 16900L),(32L, 18300L),(33L, 16900L),(34L, 18300L),(35L, 17000L),(36L, 16900L),(37L, 18300L),(38L, 16900L),(39L, 18300L),(40L, 16900L),(41L, 17000L),(42L, 16900L),(43L, 18300L),(44L, 17000L),(45L, 16900L),(46L, 18300L),(47L, 16900L),(48L, 17000L),(49L, 16900L),(50L, 17000L),(51L, 16900L),(52L, 18300L),(53L, 16900L),(54L, 18300L),(55L, 16900L),(56L, 17000L),(57L, 18300L),(58L, 16900L),(59L, 18300L),(60L, 16900L),(61L, 17000L),(62L, 18300L),(63L, 16900L),(64L, 17000L),(65L, 18300L),(66L, 16900L),(67L, 17000L),(68L, 16900L),(69L, 18300L),(70L, 17000L),(71L, 18300L),(72L, 16900L),(73L, 17000L))
    ).forall(EntryExtractingActor.weird) should be (right = true)
  }
}
