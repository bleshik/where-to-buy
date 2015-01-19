name := "Bills"

version := "1.0"

scalaVersion := "2.11.2"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

lazy val extractor = RootProject(file("../extractor"))

lazy val api = project.in(file(".")).dependsOn(extractor).aggregate(extractor)

mainClass in Compile := Some("bills.rest.Boot")

libraryDependencies += "org.mongodb" %% "casbah" % "2.7.3"

libraryDependencies += "com.novus" %% "salat" % "1.9.9"

libraryDependencies += "org.apache.commons" % "commons-lang3" % "3.3.2"

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.7"

libraryDependencies += "net.codingwell" %% "scala-guice" % "4.0.0-beta5"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.1" % "test"

libraryDependencies += "com.github.fakemongo" % "fongo" % "1.5.7" % "test"

libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.4.3"

libraryDependencies += "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.4.3"

libraryDependencies += "net.nikore.etcd" %% "scala-etcd" % "0.7"

libraryDependencies ++= {
  val akkaV = "2.3.8"
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
