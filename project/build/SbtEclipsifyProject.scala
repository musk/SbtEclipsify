import sbt._
import de.element34.sbteclipsify._

class SbtEclipsifyPluginProject(info: ProjectInfo) extends PluginProject(info) { // with SbtEclipsifyPlugin {
  override def compileOptions =  super.compileOptions ++ (Unchecked :: Deprecation :: Nil)
  override def mainResources = super.mainResources +++ "NOTICE" +++ "LICENSE" +++ (path("licenses") * "*")

  lazy val scalaTest = "org.scalatest" % "scalatest" % "1.0"
//  lazy val packageLicenses = task {
//	  log.info("Creating LICENSE texts...")
//
//	  if(licensePath.exists) FileUtilities.clean(licensePath, log)
//	  FileUtilities.copyDirectory("licenses", licensePath, log)
//	  FileUtilities.copyFile("LICENSE", this.asInstanceOf[MavenStyleScalaPaths].mainResourcesPath / "LICENSE", log)
//  }
//
//  override def packageAction = super.packageAction dependsOn { packageLicenses }
}