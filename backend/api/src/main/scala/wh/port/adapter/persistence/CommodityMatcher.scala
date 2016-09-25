package wh.port.adapter.persistence

import java.io.{ByteArrayInputStream, InputStream, InputStreamReader, StringReader}
import java.lang.Math.{max, min}
import java.util.regex.Pattern

import org.apache.commons.lang3.StringUtils.getJaroWinklerDistance
import org.supercsv.io.CsvListReader
import org.supercsv.prefs.CsvPreference
import weka.classifiers.functions.MultilayerPerceptron
import weka.core.converters.ConverterUtils.DataSource
import weka.core.{Instance, Instances}
import wh.inventory.domain.model.{Shop, Commodity, Entry}

class CommodityMatcher(val split: Double = 1) {
  private val kilos = List(("кг", "г"), ("kg", "г"), ("л", "мл"), ("", "гр"), ("", "%"), ("", "шт"), ("см", "мм"), ("м", "мм"))
  private val quantityPattern = Pattern.compile(s"\\d+([\\.,]\\d+)?\\s*(${kilos.flatMap(p => Set(p._1, p._2)).filter(s => s.nonEmpty && !s.equals("%")).mkString("|")})")
  private val percentsPattern = Pattern.compile("\\d+([\\.,]\\d+)?\\s*[%]")
  private val numberPattern = Pattern.compile("\\d+")
  private val quotes = "\"\\\\'“”‘’«»"
  private val namePattern = Pattern.compile(s"[$quotes].*[$quotes]")
  private val commentsPattern = Pattern.compile("\\(.*\\)")

  val classifier = new MultilayerPerceptron
  var data: Instances = null
  var normalization: Double = 1
  val matchingPairs = readCommodities("commodity-matching-pairs.csv")
  val notMatchingPairs = readCommodities("commodity-not-matching-pairs.csv")

  learn(new ByteArrayInputStream(
      toDataSet(matchingPairs.take((matchingPairs.size * split).asInstanceOf[Int]).map(p => scores(p._1, p._2) :+ 1) ++
      notMatchingPairs.take((notMatchingPairs.size * split).asInstanceOf[Int]).map(p => scores(p._1, p._2) :+ 0)).getBytes()))

  def toDataSet(dataSet: List[List[Any]]): String = {
    val result = new StringBuilder
    result.append("@RELATION \"commodities\"\n")
    1.to(dataSet(0).size - 1).map(x => "@ATTRIBUTE x" + (x - 1) + " NUMERIC\n").foreach(result.append)
    result.append("@ATTRIBUTE class {1, 0}\n")
    result.append("@DATA\n")
    result.toString() + dataSet.map(row => row.mkString(",")).mkString("\n")
  }

  private def learn(dataSet: InputStream): Unit = {
    val source = new DataSource(dataSet)
    data = source.getDataSet
    data.setClassIndex(data.numAttributes() - 1)
    classifier.buildClassifier(data)
    val same = new Commodity(Shop("Same", "Same"), "Same", 1)
    normalization = classifier.distributionForInstance(
      new Instance(0, scores(same, same).toArray) {
        setDataset(data)
      }
    )(0)
  }

  def scores(one: Commodity, two: Commodity): List[Double] = {
    List(kindScore(one, two), nameScore(one, two), priceScore(one, two)) ++ attributesScores(one, two)
  }

  private def priceScore(one: Commodity, two: Commodity): Double = {
    eachEntriesPair(one, two,
      (o, t) => min(o.price, t.price).asInstanceOf[Double] / max(o.price, t.price).asInstanceOf[Double])
  }

  private def nameScore(one: Commodity, two: Commodity): Double = {
    eachEntriesPair(one, two, (o, t) =>
      sentencesDifference(name(o.shopSpecificName, o.shop), name(t.shopSpecificName, t.shop, Some(name(o.shopSpecificName, o.shop)))))
  }

  private def kindScore(one: Commodity, two: Commodity): Double = {
    eachEntriesPair(one, two, (o, t) =>
        if (sentencesDifference(kind(o.shopSpecificName, o.shop), kind(t.shopSpecificName, t.shop)) > 0.9) 1.0 else 0.0)
  }

  private def quantityScore(one: Commodity, two: Commodity): Double = {
    eachEntriesPair(one, two, (o, t) =>
      if (quantity(o.shopSpecificName, o.shop).equals(quantity(t.shopSpecificName, t.shop))) 1.0 else 0.0)
  }

  private def percentScore(one: Commodity, two: Commodity): Double = {
    eachEntriesPair(one, two, (o, t) =>
      if (percent(o.shopSpecificName, o.shop).equals(percent(t.shopSpecificName, t.shop))) 1.0 else 0.0)
  }

  private def attributesScores(one: Commodity, two: Commodity): List[Double] = {
    def score(exclude: Boolean): Double = {
      eachEntriesPair(one, two, { (o, t) =>
        val attributesOne = attributes(o.shopSpecificName, o.shop)
        val attributesTwo = attributes(t.shopSpecificName, t.shop, Some(name(o.shopSpecificName, o.shop)))
        if (exclude) {
          sentencesDifference(numbers(attributesOne, exclude = true).split("\\s").filter(_.size > 3).mkString(" "),
            numbers(attributesTwo, exclude = true).split("\\s").filter(_.size > 3).mkString(" "))
        } else {
          sentencesDifference(numbers(attributesOne), numbers(attributesTwo), exact = true)
        }
      })
    }
    List(score(exclude = true), score(exclude = false))
  }

  private def numbers(str: String, exclude: Boolean = false): String = {
    var noNumbers = str
    val numbers = new StringBuilder
    val m = numberPattern.matcher(str)
    while(m.find()) {
      numbers.append(m.group).append(" ")
      noNumbers = noNumbers.replace(m.group, "")
    }
    if (exclude) {
      noNumbers
    } else {
      numbers.toString()
    }
  }

  private def sentencesDifference(one: String, two: String, exact: Boolean = false): Double = {
    val wordsOne = sanitizeName(one).split("\\s").filter(_.nonEmpty).toList
    val wordsTwo = sanitizeName(two).split("\\s").filter(_.nonEmpty).toList

    def calculateScore (oneWords: List[String], twoWords: List[String]): List[Double] = {
      if (oneWords.isEmpty) {
        List.fill(twoWords.size)(0.0)
      } else if (twoWords.isEmpty) {
        List.fill(oneWords.size)(0.0)
      } else {
        val scores = oneWords.map { w0 =>
          val localScores = twoWords.map(w1 => if (exact) if (w0.equals(w1)) 1.0 else 0.0 else distance(w0, w1))
          (localScores.max, localScores.indexOf(localScores.max))
        }
        val maxScoreIndex = scores.indexWhere(p => p._1 == scores.map(_._1).max)
        val maxScore = scores(maxScoreIndex)
        List(maxScore._1) ++ calculateScore(
          oneWords.take(maxScoreIndex) ++ oneWords.drop(maxScoreIndex + 1),
          twoWords.take(maxScore._2) ++ twoWords.drop(maxScore._2 + 1)
        )
      }
    }
    val s = calculateScore(wordsOne, wordsTwo)
    if (s.size > 0) s.sum / s.size else 1.0
  }

  private def distance(one: String, two: String): Double = {
    if (one.size == 0 || two.size == 0 || one.equals(two)) {
      1
    } else {
      getJaroWinklerDistance(one, two)
    }
  }

  private def eachEntriesPair(one: Commodity, two: Commodity, f: (Entry, Entry) => Double): Double = {
    one.entries.map(o =>
      two.entries.map(t =>
        Math.max(f(o, t), f(t, o))
      ).max
    ).max
  }

  /**
   * Returns whether two commodities are similar with the given confidence.
   * @param one a commodity.
   * @param two a commodity.
   * @param confidence a confidence, from 0 to 1.
   * @return whether two commodities are similar.
   */
  def matching(one: Commodity, two: Commodity, confidence: Double = 0.5): Boolean = {
    matchingConfidence(one, two) > confidence
  }

  /**
   * Returns a number from 0 to 1, where 1 means that the commodities are perfectly matching.
   * @param one a commodity.
   * @param two a commodity.
   * @return a confidence, from 0 to 1.
   */
  def matchingConfidence(one: Commodity, two: Commodity): Double = {
    if (quantityScore(one, two) <= 0 || percentScore(one, two) <= 0) {
      0
    } else {
      Math.min(classifier.distributionForInstance(
        new Instance(0, scores(one, two).toArray) {
          setDataset(data)
        }
      )(0) / normalization, 1.0)
    }
  }

  def sanitizeName(name: String): String =
    if (name == null) {
      ""
    } else {
      name.map({c => if (c.isLetterOrDigit || c.isSpaceChar || c.isWhitespace) c else ' '})
        .trim
        .replaceAll("\\s\\s+", " ")
        .toLowerCase
    }

  private def preprocessTitle(title: String, shop: Shop): String = {
    shop.name match {
      case "Cont" => commentsPattern.matcher(title).replaceAll(" ")
      case _ => title
    }
  }

  private def toCanonicalQuantity(quantity: String): String = {
    val number = BigDecimal(quantity.replaceAll(kilos.flatMap(p => Set(p._1, p._2)).filter(s => s.nonEmpty).mkString("|"), "").replace(",", ".").trim).underlying().stripTrailingZeros()
    val kg = kilos.find(p => p._1.nonEmpty && quantity.contains(p._1) || p._2.nonEmpty && quantity.contains(p._2))
    kg.map(p =>
      if (Pattern.compile("[^a-zA-Zа-яА-Я]*" + p._1 + "$").matcher(quantity).matches() && p._1.nonEmpty) {
        number.multiply(BigDecimal(if (p._1.equals("см")) 100 else 1000).underlying()).stripTrailingZeros().toPlainString + p._2
      } else {
        number.toPlainString + p._2
      }
    ).getOrElse(number.toPlainString)
  }

  def titleTokens(title: String, shop: Shop, probableName: Option[String] = None): TitleTokens = {
    val preprocessedTitle = preprocessTitle(title, shop)
    val tokens = preprocessedTitle.split("\\s+")

    def hasUpperCase(s: String): Boolean = s.exists(c => c.isUpper)

    val kind = tokens.head.trim

    var name = tokens.tail.mkString(" ")
    val quantity = new StringBuilder
    val quantityMatcher = quantityPattern.matcher(preprocessedTitle)
    while (quantityMatcher.find()) {
      name = name.replace(quantityMatcher.group, "")
      quantity.append(toCanonicalQuantity(quantityMatcher.group.trim))
    }

    val percent = new StringBuilder
    val percentMatcher = percentsPattern.matcher(preprocessedTitle)
    while(percentMatcher.find()) {
      name = name.replace(percentMatcher.group, "")
      percent.append(toCanonicalQuantity(percentMatcher.group.trim))
    }

    var attributes: String = ""
    name = probableName.flatMap {t =>
      if (t.trim.nonEmpty) {
        val probableNameTokens = t.toLowerCase.split("\\s+")
        if (probableNameTokens.forall(t => name.toLowerCase.contains(t))) {
          attributes = name.toLowerCase
          probableNameTokens.foreach(n => attributes = attributes.replaceAll(Pattern.quote(n), ""))
          attributes = attributes.split("\\s+").mkString(" ")
          Some(t)
        } else {
          None
        }
      } else {
        None
      }
    }.getOrElse({
      val nameTokens = name.split("\\s+")
      val m = namePattern.matcher(name)
      var nameCandidate = ""
      attributes = nameTokens.mkString(" ")
      if (m.find()) {
        nameCandidate = m.group()
      } else {
        nameCandidate = nameTokens.filter(hasUpperCase).mkString(" ").trim
        if (nameCandidate.nonEmpty) {
          attributes = nameTokens.filterNot(hasUpperCase).mkString(" ")
        }
      }
      attributes = attributes.replace(nameCandidate, "")
      nameCandidate
    })

    TitleTokens(kind, name, quantity.toString(), percent.toString(), attributes)
  }

  private def kind(title: String, shop: Shop, probableName: Option[String] = None): String = titleTokens(title, shop, probableName).kind
  private def name(title: String, shop: Shop, probableName: Option[String]= None): String = titleTokens(title, shop, probableName).name
  private def attributes(title: String, shop: Shop, probableName: Option[String] = None): String = titleTokens(title, shop, probableName).attributes
  private def quantity(title: String, shop: Shop, probableName: Option[String] = None): String = titleTokens(title, shop, probableName).quantity
  private def percent(title: String, shop: Shop, probableName: Option[String] = None): String = titleTokens(title, shop, probableName).percent

  private def readCommodities(resource: String): List[(Commodity, Commodity)] = {
    val reader = new CsvListReader(new InputStreamReader(getClass.getClassLoader.getResourceAsStream(resource), "UTF-8"), CsvPreference.STANDARD_PREFERENCE)
    def extractCommodities(reader: CsvListReader): Stream[Commodity] = {
      val row: java.util.List[String] = reader.read()
      if (row != null) {
        new Commodity(Shop(row.get(0).trim, "Москва"), row.get(1).trim, row.get(2).trim.toInt) #:: extractCommodities(reader)
      } else {
        Stream.empty
      }
    }
    val commodities = extractCommodities(reader).toList.zipWithIndex
    commodities.filter(_._2 % 2 != 0).map(_._1).zip(commodities.filter(_._2 % 2 == 0).map(_._1))
  }

  case class TitleTokens(kind: String, name: String, quantity: String, percent: String, attributes: String)
 }
