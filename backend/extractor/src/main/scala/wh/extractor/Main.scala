package wh.extractor

import java.net.URL

import akka.actor.ActorSystem
import com.typesafe.config.{ConfigFactory, ConfigParseOptions, ConfigResolveOptions, ConfigValueFactory}
import com.typesafe.scalalogging.LazyLogging
import wh.extractor.cont.ContExtractor
import wh.extractor.komus.KomusExtractor
import wh.extractor.utkonos.UtkonosExtractor

object Main extends LazyLogging {
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
      Thread.sleep(3 * 60 * 60 * 1000)
    }

    logger.info("Exiting...")
  }

  private def upload(output: String): Unit = {
    logger.debug(s"My payload is ${payload.map(_._1)}")
    payload.par.foreach { p =>
      doUpload(p._2.extract(new URL(p._1)), output)
    }
  }

  private def doUpload(iterator: Iterator[ExtractedEntry], output: String): Unit = {
    logger.info("Started uploading to " + output)
    output match {
      case "console" => iterator.foreach(println)
      case "akka"    =>
        val akkaEndpoint = Environment.akkaEndpoint
        val whereToBuySystem = s"akka.tcp://WhereToBuySystem@$akkaEndpoint/user/EntryExtractingActor"
        logger.info("Extractor will send extracted data to " + whereToBuySystem)
        val remote = extractorSystem.actorSelection(whereToBuySystem)
        iterator.foreach(remote ! _)
    }
  }

  private def payload: List[(String, Extractor)] = {
    val all = List(
        ("http://www.utkonos.ru/cat", new UtkonosExtractor),
        ("http://www.komus.ru/catalog/6311/", new KomusExtractor),
        ("http://www.7cont.ru", new ContExtractor)
    )
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
