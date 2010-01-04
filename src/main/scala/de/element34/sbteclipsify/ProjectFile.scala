package de.element34.sbteclipsify

import sbt._

import java.io.File
import java.nio.charset.Charset._

class ProjectFile(project: Project, log: Logger) {
  def writeFile: Option[String] = {
    
    val scalaBuilder = "ch.epfl.lamp.sdt.core.scalabuilder"
    val scalaNature = "ch.epfl.lamp.sdt.core.scalanature"
    val javaNature = "org.eclipse.jdt.core.javanature"

    lazy val projectFile: File = project.info.projectPath / ".project" asFile
    lazy val projectContent = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n" +
<projectDescription>
  <name>{project.projectName.get.getOrElse("")}</name>
  <comment>{getProjectDescription}</comment>
  <projects>{createSubProjects}</projects>
  <buildSpec>
    <buildCommand>
      <name>{scalaBuilder}</name>
    </buildCommand>
  </buildSpec>
  <natures>
    <nature>{scalaNature}</nature>
    <nature>{javaNature}</nature>
  </natures>
</projectDescription>

    def getProjectDescription =  project.asInstanceOf[SbtEclipsifyPlugin].projectDescription.value

    def createSubProjects = ""

    FileUtilities.touch(projectFile, log) match {
      case Some(error) =>
	Some("Unable to write project file " + projectFile+ ": " + error)
      case None => 
	FileUtilities.write(projectFile, projectContent, forName("UTF-8"), log)
    }
  }
}

object ProjectFile {
  def apply(project: Project, log: Logger) = new ProjectFile(project, log)
}
