name := "Bills"

version := "1.0"

scalaVersion := "2.11.2"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

mainClass in Compile := Some("bills.rest.Boot")

libraryDependencies += "org.mongodb" %% "casbah" % "2.7.3"

libraryDependencies += "com.novus" %% "salat" % "1.9.9"

libraryDependencies += "org.apache.commons" % "commons-lang3" % "3.3.2"

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.7"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.1" % "test"

libraryDependencies += "com.github.fakemongo" % "fongo" % "1.5.7" % "test"

Revolver.settings
