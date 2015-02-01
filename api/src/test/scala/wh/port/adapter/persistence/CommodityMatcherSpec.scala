package wh.port.adapter.persistence

import org.scalatest.{Matchers, FlatSpec}
import wh.domain.model.Commodity

class CommodityMatcherSpec extends FlatSpec with Matchers {
  val matcher = new CommodityMatcher(0.5)

  it should "parse canonical quantity" in {
    matcher.titleTokens("Нож кухонный  Samura Eco  универ.12,5см черная керам.SC-0021B", "Utkonos").quantity should be("1250мм")
  }

  it should "match similar products" in {
    val wrong = matcher.matchingPairs.takeRight(matcher.matchingPairs.size / 2).filter(p => !matcher.matching(p._1, p._2))
    val trueNegative = wrong.size / (matcher.matchingPairs.size / 2).asInstanceOf[Double]
    if (trueNegative > 0.5) {
      fail("Too many true negative pairs: " + trueNegative + "\n" + wrong.mkString("\n"))
    }
  }

  it should "not match different products" in {
    val wrong = matcher.notMatchingPairs.takeRight(matcher.notMatchingPairs.size / 2).filter(p => matcher.matching(p._1, p._2))
    val falsePositive = wrong.size / (matcher.notMatchingPairs.size / 2).asInstanceOf[Double]
    if (falsePositive > 0.05) {
      fail("Too many false positive pairs: " + falsePositive + "\n" + wrong.mkString("\n"))
    }
  }

  private def pairToString(p: (Commodity, Commodity)): String = {
    s"""
      |========================
      |${matcher.scores(p._1, p._2)}
      |${p._1.name}
      |${p._1.entries.head.price}
      |${p._2.name}
      |${p._2.entries.head.price}
      |========================
    """.stripMargin
  }
}
