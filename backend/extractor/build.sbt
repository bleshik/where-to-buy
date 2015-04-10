name := "Wh Extractor"

version := "1.0"

scalaVersion := "2.11.5"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

enablePlugins(JavaAppPackaging)

mainClass in Compile := Some("wh.application.extractor.ExtractorApp")

resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"

libraryDependencies += "net.sourceforge.htmlunit" % "htmlunit" % "2.15"

libraryDependencies += "org.jsoup" % "jsoup" % "1.8.1"

libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.4.3"

libraryDependencies += "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.4.3"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.1" % "test"

libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0"

libraryDependencies += "org.slf4j" % "jcl-over-slf4j" % "1.7.12"

libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.7.12"

libraryDependencies ++= {
  val akkaV = "2.4-SNAPSHOT"
  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaV,
    "com.typesafe.akka" %% "akka-remote" % akkaV
  )
}
