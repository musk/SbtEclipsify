import sbt._
import de.element34.sbteclipsify._

class SbtEclipsifyPluginProject(info: ProjectInfo) extends PluginProject(info) { // with SbtEclipsifyPlugin {
  override def compileOptions =  super.compileOptions ++ (Unchecked :: Deprecation :: Nil)
  override def mainResources = super.mainResources +++ "NOTICE" +++ "LICENSE" +++ (path("licenses") * "*")

  lazy val scalaTest = "org.scalatest" % "scalatest" % "1.0"
//  lazy val eclipse = "de.element34" % "sbt-eclipsify" % "0.4.0"

  override def managedStyle = ManagedStyle.Maven
  val publishTo = "Scala Tools Nexus" at "http://nexus.scala-tools.org/content/repositories/releases/"
  Credentials(Path.userHome / ".ivy2" / ".credentials", log)

  // override def packageDocsJar = defaultJarPath("-javadoc.jar")
//   override def packageSrcJar= defaultJarPath("-sources.jar")
//   val sourceArtifact = Artifact(artifactID, "src", "jar", Some("sources"), Nil, None)
//   val docsArtifact = Artifact(artifactID, "docs", "jar", Some("javadoc"), Nil, None)
//   override def packageToPublishActions = super.packageToPublishActions ++ Seq(packageDocs, packageSrc)
}
