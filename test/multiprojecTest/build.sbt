import de.element34.sbteclipsify._
import ProjectType._

description := "This is a test description"

nature := ScalaPlugin combine SbtEclipseIntegrationNature

defaultExcludes := new sbt.SimpleFileFilter((f:File) => f.getName.endsWith("scala-library.jar"))

