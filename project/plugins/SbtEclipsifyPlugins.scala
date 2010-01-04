import sbt._

class SbtEclipsifyPlugin(info: ProjectInfo) extends PluginDefinition(info) {
  lazy val eclipsify = "de.element34" % "sbteclipsify" % "0.2.0"
}
