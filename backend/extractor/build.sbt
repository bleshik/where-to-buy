import scala.collection.JavaConverters._

name := "Wh Extractor"

version := "1.0"

scalaVersion := "2.11.5"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

initialize := {
  val required = "1.8"
  val current  = sys.props("java.specification.version")
  assert(current == required, s"Unsupported JDK: java.specification.version $current != $required")
}

enablePlugins(JavaAppPackaging)

mainClass in Compile := Some("wh.application.extractor.aws.ExtractorLambda")

libraryDependencies += "org.jsoup" % "jsoup" % "1.8.1"

libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.7.5"

libraryDependencies += "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.7.5"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.1" % "test"

libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0"

libraryDependencies += "org.slf4j" % "jcl-over-slf4j" % "1.7.21"

libraryDependencies += "org.slf4j" % "slf4j-log4j12" % "1.7.21"

libraryDependencies += "log4j" % "log4j" % "1.2.17"

//libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.7.12"

libraryDependencies += "org.scala-lang.modules" %% "scala-java8-compat" % "0.7.0"

libraryDependencies += "com.amazonaws" % "aws-lambda-java-core" % "1.1.0" exclude("commons-logging", "commons-logging")

libraryDependencies += "com.amazonaws" % "aws-java-sdk-sns" % "1.11.28" exclude("commons-logging", "commons-logging")

libraryDependencies += "com.amazonaws" % "aws-java-sdk-iam" % "1.11.29" exclude("commons-logging", "commons-logging")

libraryDependencies += "com.amazonaws" % "aws-lambda-java-events" % "1.3.0" exclude("commons-logging", "commons-logging")

libraryDependencies += "com.amazonaws" % "aws-lambda-java-log4j" % "1.0.0"

//retrieveManaged := true

enablePlugins(AwsLambdaPlugin)

lambdaHandlers := Seq(
  "ExtractorLambda" -> "wh.application.extractor.aws.ExtractorLambda::extract"
)

test in assembly := {}

s3Bucket := Some("wh-prod")

awsLambdaTimeout := Some(30)

region := Some("eu-central-1")

roleArn := Some("arn:aws:iam::034173546782:role/lambda")

