package de.element34.sbteclipsify

import sbt._

trait SbtEclipsifyPlugin extends Project {
  lazy val eclipse = task {
    log.info("Creating eclipse project...")
    writeProjectFile(log) match {
      case None => writeClasspathFile(log)
      case ret @ Some(_) => ret
    }
  }

  lazy val projectDescription = propertyOptional[String](projectName.value + " " + projectVersion.value)
  lazy val includeProject = propertyOptional[Boolean](false)
  lazy val includePlugin = propertyOptional[Boolean](false)
  lazy val sbtDependency = propertyOptional[Boolean](false)

  def writeClasspathFile(log: Logger): Option[String] = ClasspathFile(this, log).writeFile
  def writeProjectFile(log: Logger): Option[String] = ProjectFile(this, log).writeFile
}
