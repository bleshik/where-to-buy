name := "Where To Buy"

version := "1.0"

scalaVersion := "2.11.5"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

lazy val extractor = RootProject(file("../extractor"))

lazy val api = project.in(file(".")).dependsOn(extractor).aggregate(extractor)

mainClass in Compile := Some("wh.application.Boot")

resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"

libraryDependencies += "org.mongodb" %% "casbah" % "2.7.3"

libraryDependencies += "com.novus" %% "salat" % "1.9.9"

libraryDependencies += "org.apache.commons" % "commons-lang3" % "3.3.2"

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.7"

libraryDependencies += "net.codingwell" %% "scala-guice" % "4.0.0-beta5"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.1" % "test"

libraryDependencies += "com.github.fakemongo" % "fongo" % "1.5.10" % "test"

libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.4.3"

libraryDependencies += "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.4.3"

libraryDependencies += "org.mousio" % "etcd4j" % "2.1.1"

libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0"

libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.7.10"

libraryDependencies += "nz.ac.waikato.cms.weka" % "weka-stable" % "3.6.12"

libraryDependencies += "nz.ac.waikato.cms.weka" % "LibSVM" % "1.0.6" exclude("nz.ac.waikato.cms.weka", "weka-dev")

libraryDependencies += "net.sf.supercsv" % "super-csv" % "2.2.1"

libraryDependencies ++= {
  val akkaV = "2.4-SNAPSHOT"
  val sprayV = "1.3.1"
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

Revolver.settings
