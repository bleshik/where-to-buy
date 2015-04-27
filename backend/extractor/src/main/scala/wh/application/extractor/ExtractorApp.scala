package wh.application.extractor

import java.net.URL
import java.util.Collections

import akka.actor.ActorSystem
import com.typesafe.config.{ConfigFactory, ConfigParseOptions, ConfigResolveOptions, ConfigValueFactory}
import com.typesafe.scalalogging.LazyLogging
import wh.application.extractor.auchan.AuchanExtractor
import wh.application.extractor.av.AvExtractor
import wh.application.extractor.cont.ContExtractor
import wh.application.extractor.dixy.DixyExtractor
import wh.application.extractor.globusgurme.GlobusGurmeExtractor
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
      .withValue("logDeadLetters", ConfigValueFactory.fromAnyRef(Environment.logDeadLetters))
      .resolve())
  }
  private lazy val remote = {
    logger.info(s"Extractor will upload entries to ${Environment.akkaEndpoint}")
    extractorSystem.actorSelection(Environment.akkaEndpoint)
  }

  def main(args: Array[String]): Unit = {
    if (args.length < 1) {
      throw new IllegalArgumentException("You should specify the output: 'console' or 'akka'")
    }

    logger.info("Started extractor with args: " + args.mkString(" "))

    upload(args.head)

    logger.info("Exiting...")
  }

  private def upload(output: String): Unit = {
    val myPayload = payload.par.withMinThreads(Environment.minimumConcurrency)
    while(true) {
      myPayload.foreach { it =>
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
      case "akka" => remote ! entry
    }
  }

  private def payload: List[Iterator[ExtractedEntry]] = {
    val doneSources = Collections.synchronizedMap(new java.util.IdentityHashMap[Any, Any]())
    @volatile var sourcesAmount = 0
    val sources = part(List(
      ("http://av.ru/food/all/", new AvExtractor),
      ("http://av.ru/nonfood/", new AvExtractor),
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
      p._2.parts(new URL(p._1)).map { itFn =>
        () => {
          val it = itFn()
          new Iterator[ExtractedEntry] {
            override def hasNext: Boolean = {
              val n = it.hasNext
              if (!n) {
                if (!doneSources.containsKey(itFn)) {
                  doneSources.put(itFn, "DONE")
                  val done = doneSources.size
                  if (done < sourcesAmount) {
                    logger.info(s"Finished first round of extracting of a source: $done/$sourcesAmount")
                  } else {
                    logger.info(s"Finished first round of extracting of all $sourcesAmount sources")
                  }
                }
              }
              n
            }

            override def next(): ExtractedEntry = it.next()
          }
        }
      }
    })
    sourcesAmount = sources.length
    logger.info(s"My payload contains ${sourcesAmount} sources")
    sources.grouped(Math.max(sources.length / Environment.minimumConcurrency, 1)).toList
      .map { partsChunk => { () => partsChunk.iterator.flatMap(_()) } } // merge chunk into one part
      .map { p => Iterator.continually(p()).flatten } // make every merged chunk result infinite
  }

  private def part[T](all: List[T]) =
    if (Environment.instance <= Environment.instances) {
      val part = all.size / Environment.instances
      val tail = all.takeRight(all.size % Environment.instances)
      all.drop((Environment.instance - 1) * part).take(part) ++ (if (Environment.instance.equals(Environment.instances)) tail else List())
    } else {
      logger.warn("Environment is not correct. The instance number is out of bounds. " + Environment.instance + "/" + Environment.instances)
      all
    }
}
