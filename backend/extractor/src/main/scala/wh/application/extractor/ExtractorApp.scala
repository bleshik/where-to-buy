package wh.application.extractor

import java.net.URL

import akka.actor.ActorSystem
import com.typesafe.config.{ConfigFactory, ConfigParseOptions, ConfigResolveOptions, ConfigValueFactory}
import com.typesafe.scalalogging.LazyLogging
import wh.application.extractor.auchan.AuchanExtractor
import wh.application.extractor.cont.ContExtractor
import wh.application.extractor.dixy.DixyExtractor
import wh.application.extractor.infrastructure.Environment
import wh.application.extractor.komus.KomusExtractor
import wh.application.extractor.metro.MetroExtractor
import wh.application.extractor.utkonos.UtkonosExtractor
import wh.extractor.domain.model.ExtractedEntry
import wh.util.ConcurrencyUtil._

object ExtractorApp extends LazyLogging {
  private lazy val extractorSystem = {
    val extractorAddress = Environment.balancerIp.orElse(Environment.privateIp).getOrElse("127.0.0.1")
    ActorSystem("ExtractorSystem", ConfigFactory.load("extractor", ConfigParseOptions.defaults(), ConfigResolveOptions.defaults.setAllowUnresolved(true))
      .withValue("hostname", ConfigValueFactory.fromAnyRef(extractorAddress))
      .resolve())
  }

  def main(args: Array[String]): Unit = {
    if (args.length < 1) {
      throw new IllegalArgumentException("You should specify the output: 'console' or 'akka'")
    }

    logger.info("Started extractor with args: " + args.mkString(" "))

    while(true) {
      upload(args.head)
    }

    logger.info("Exiting...")
  }

  private def upload(output: String): Unit = {
    val myPayload = payload
    logger.info(s"My payload contains ${myPayload.size} sources")
    while(true) {
      payload.par.withMinThreads(Environment.minimumConcurrency).foreach { it =>
        if (it.hasNext) {
          doUpload(it.next(), output)
        }
      }
    }
  }

  private def doUpload(entry: ExtractedEntry, output: String): Unit = {
    output match {
      case "none" =>
      case "console" => println(entry)
      case "akka"    =>
        val remote = extractorSystem.actorSelection(Environment.akkaEndpoint)
        remote ! entry
    }
  }

  private def payload: List[Iterator[ExtractedEntry]] = {
    val all = List(
      ("http://klg.metro-cc.ru", new MetroExtractor),
      ("http://www.auchan.ru", new AuchanExtractor),
      ("http://www.utkonos.ru/cat", new UtkonosExtractor),
      ("http://www.komus.ru/catalog/6311/", new KomusExtractor),
      ("http://www.7cont.ru", new ContExtractor),
      ("http://dixy.ru", new DixyExtractor),
      ("http://globusgurme.ru/catalog", new GlobusGurmeExtractor)
    ).filter { p =>
      Environment.shops.isEmpty || Environment.shops.get.exists { shop => p._1.toLowerCase.contains(shop.toLowerCase) }
    }.flatMap { p =>
      p._2.parts(new URL(p._1)).map { part => Iterator.continually(part()).flatten } // make every extractor result infinite
    }

    if (Environment.instance <= Environment.instances) {
      val part = all.size / Environment.instances
      val tail = all.takeRight(all.size % Environment.instances)
      all.drop((Environment.instance - 1) * part).take(part) ++ (if (Environment.instance.equals(Environment.instances)) tail else List())
    } else {
      logger.warn("Environment is not correct. The instance number is out of bounds. " + Environment.instance + "/" + Environment.instances)
      all
    }
  }
}
