sbtPlugin := true

name := "sbt-eclipsify"

version := "0.10.0-SNAPSHOT"

organization := "de.element34"

scalaVersion := "2.8.1"

publishTo <<= (version) { version: String =>
  val nexus = "http://nexus.scala-tools.org/content/repositories/"
  if (version.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus+"snapshots/") 
  else                                   Some("releases" at nexus+"releases/")
}

credentials += Credentials(Path.userHome / ".credentials")
