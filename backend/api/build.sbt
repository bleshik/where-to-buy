name := "Wh Api"

version := "1.0"

scalaVersion := "2.11.5"

initialize := {
  val required = "1.8"
  val current  = sys.props("java.specification.version")
  assert(current == required, s"Unsupported JDK: java.specification.version $current != $required")
}

ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) }

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

lazy val extractor = RootProject(file("../extractor"))

lazy val dynamodb = RootProject(file("../ddd/ddd-dynamodb"))

lazy val api = project.in(file(".")).dependsOn(extractor).dependsOn(dynamodb)

mainClass in Compile := Some("wh.application.ApiApp")

resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"

resolvers += "Bintray Repository" at "http://dl.bintray.com/augi/maven"

libraryDependencies += "com.novus" %% "salat" % "1.9.9"

libraryDependencies += "org.apache.commons" % "commons-lang3" % "3.3.2"

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.7"

libraryDependencies += "net.codingwell" %% "scala-guice" % "4.1.0"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.1" % "test"

libraryDependencies += "org.mousio" % "etcd4j" % "2.1.1"

libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0"

libraryDependencies += "nz.ac.waikato.cms.weka" % "weka-stable" % "3.6.12"

libraryDependencies += "nz.ac.waikato.cms.weka" % "LibSVM" % "1.0.6" exclude("nz.ac.waikato.cms.weka", "weka-dev")

libraryDependencies += "net.sf.supercsv" % "super-csv" % "2.2.1"

libraryDependencies += "org.scala-lang.modules" %% "scala-java8-compat" % "0.7.0"

libraryDependencies += "com.amazonaws" % "aws-java-sdk-s3" % "1.11.35"

libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.8.2"

libraryDependencies += "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.8.2"

libraryDependencies ++= {
  val akkaV = "2.3.9"
  val sprayV = "1.3.3"
  Seq(
    "io.spray"            %%  "spray-can"     % sprayV,
    "io.spray"            %%  "spray-routing" % sprayV,
    "io.spray"            %%  "spray-testkit" % sprayV  % "test",
    "com.typesafe.akka"   %%  "akka-actor"    % akkaV,
    "com.typesafe.akka"   %%  "akka-remote"    % akkaV,
    "com.typesafe.akka"   %%  "akka-testkit"  % akkaV   % "test",
    "org.specs2"          %%  "specs2-core"   % "2.3.11" % "test"
  )
}

enablePlugins(AwsLambdaPlugin)

lambdaHandlers := Seq(
  "ExtractedEntryHandler" -> "wh.application.ExtractedEntryHandlerHelper"
)

test in assembly := {}

s3Bucket := Some("wh-prod")

awsLambdaTimeout := Some(30)

region := Some("eu-central-1")

roleArn := Some("arn:aws:iam::034173546782:role/lambda")

