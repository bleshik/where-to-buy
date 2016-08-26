package wh.application.extractor.aws

import org.slf4j.LoggerFactory
import org.slf4j.Logger
import actor.port.adapter.aws.SnsEventTransport
import com.amazonaws.services.lambda.runtime.Context
import wh.application.extractor.ExtractorApp

object ExtractorLambda {
  private val logger: Logger = LoggerFactory.getLogger(this.getClass())
  def main(args: Array[String]): Unit = extract
  def extract: Unit = {
    ExtractorApp.extract(new SnsEventTransport("ExtractorLambda"), (entry) => println(entry))
  }
}