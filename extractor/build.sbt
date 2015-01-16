name := "Wh Extractor"

version := "1.0"

scalaVersion := "2.11.2"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

mainClass in Compile := Some("wh.extractor.Main")

libraryDependencies += "net.sourceforge.htmlunit" % "htmlunit" % "2.15"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.1" % "test"

Revolver.settings
