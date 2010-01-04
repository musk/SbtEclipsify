package de.element34.sbteclipsify

import sbt._

trait SbtEclipsifyPlugin extends Project {
  lazy val eclipsify = task {
    log.info("Creating eclipse project...")
    writeProjectFile(log) match {
      case None => writeClasspathFile(log)
      case ret @ Some(_) => ret
    }
  }

  def projectDescription: Property[String]

  def writeClasspathFile(log: Logger): Option[String] = ClasspathFile(this, log).writeFile    
  def writeProjectFile(log: Logger): Option[String] = ProjectFile(this, log).writeFile
}
