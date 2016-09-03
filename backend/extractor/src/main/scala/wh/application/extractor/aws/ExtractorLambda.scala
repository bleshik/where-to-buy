package wh.application.extractor.aws

import actor.port.adapter.aws.SnsEventTransport
import com.amazonaws.services.lambda.runtime.Context
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import wh.application.extractor.ExtractorApp

object ExtractorLambda {
  private val logger: Logger = LoggerFactory.getLogger(this.getClass())
  def extract(context: Context): Unit = {
    logger.info("Starting extracting")
    ExtractorApp.extract(
      new SnsEventTransport("SnsEventTransport"),
      (entry) => println(entry)
    )
  }
}
