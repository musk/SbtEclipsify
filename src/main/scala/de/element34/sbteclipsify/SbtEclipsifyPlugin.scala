package de.element34.sbteclipsify

import sbt._
import java.io.File
import java.nio.charset.Charset._

trait SbtEclipsifyPlugin extends Project {
  lazy val eclipsify = task {
    log.info("Creating eclipse project...")
    writeProjectFile(info, log) match {
      case None => writeClasspathFile(info, log)
      case ret: Some[String] => ret
    }
  }

  def writeClasspathFile(info: ProjectInfo, log: Logger): Option[String] = {
    lazy val classpathContent = <classpath>{writeEntries(createEntries)}</classpath>
    lazy val classpathFile: File = info.projectPath / ".classpath" asFile

    def writeEntries(entries: List[ClasspathEntry]): String = {
      ("" /: entries)(_ + _.toXml)
    }

    def createEntries: List[ClasspathEntry] = {
      def getJarEntries(path: Path): List[ClasspathEntry] = {
	val files = path.asFile.listFiles(new FileFilter() {
	  def accept(file: File) = file.getName.endsWith(".jar")
	})
	List.fromArray(files.map(file => ClasspathEntry(Variable, file.getAbsolutePath, None, Nil)))
      }

      val paths = this.asInstanceOf[BasicProjectPaths]
      var result = getJarEntries(this.asInstanceOf[BasicDependencyPaths].dependencyPath)
      result = getJarEntries(this.asInstanceOf[BasicDependencyPaths].managedDependencyPath) ++ result
      result = ClasspathEntry(Variable, paths.mainScalaSourcePath.toString, None, IncludeFilter("**/*.scala"):: Nil) :: result
      result = ClasspathEntry(Variable, paths.mainJavaSourcePath.toString, None, IncludeFilter("**/*.java"):: Nil) :: result
      result = ClasspathEntry(Variable, paths.testScalaSourcePath.toString, None, IncludeFilter("**/*.scala"):: Nil) :: result
      result = ClasspathEntry(Variable, paths.testJavaSourcePath.toString, None, IncludeFilter("**/*.scala"):: Nil) :: result
//       testResourcePath
      
//       outputPath
//       mainDocPath
//       testDocPath
//       mainCompilePath
//       testCompilePath
//       mainAnalysisPath
//       testAnalysisPath
      Nil
    }

    FileUtilities.touch(classpathFile, log) match {
      case Some(error) =>
	Some("Unable to write classpath file " + classpathFile + ": " + error)
      case None =>
	FileUtilities.write(classpathFile, classpathContent.toString, forName("UTF-8"), log)
	None
    }
  }

  def writeProjectFile(info: ProjectInfo, log: Logger): Option[String] = {
    val scalaBuilder = "ch.epfl.lamp.sdt.core.scalabuilder"
    val scalaNature = "ch.epfl.lamp.sdt.core.scalanature"
    val javaNature = "org.eclipse.jdt.core.javanature"
    
    lazy val projectFile: File = info.projectPath / ".project" asFile
    lazy val projectContent = <projectDescription>
      <name>{getOption(projectName)}</name>
      <comment>{projectDescription}</comment>
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

    def projectDescription: String = getOption(projectName) + " " + getOption(projectVersion)
    def createSubProjects = ""

    FileUtilities.touch(projectFile, log) match {
      case Some(error) =>
	Some("Unable to write project file " + projectFile+ ": " + error)
      case None => 
	FileUtilities.write(projectFile, projectContent.toString, forName("UTF-8"), log)
	None
    }
  }

  def getOption[A](opt: Property[A]): String = {
    opt.get match {
      case Some(str) => str.toString
      case None => ""
    }
  }
}
