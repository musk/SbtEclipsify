import sbt._
//import de.element34.sbteclipsify._

class SbtEclipsifyPluginProject(info: ProjectInfo) extends PluginProject(info) {
  override def compileOptions =  super.compileOptions ++ (Unchecked :: Deprecation :: Nil) 

  lazy val scalaTest = "org.scalatest" % "scalatest" % "1.0"
}
