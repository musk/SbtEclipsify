package de.element34.sbteclipsify

import scala.xml._
import sbt._

import java.io.File
import java.nio.charset.Charset._

abstract class Filter(pattern: String) {
  def mkString = "=\"" + pattern + "\""
}

case class IncludeFilter(pattern: String) extends Filter(pattern) {
  override def mkString = " including" + super.mkString
}
case class ExcludeFilter(pattern: String) extends Filter(pattern) {
  override def mkString = " excluding" + super.mkString
}

case class FilterChain(inc: Option[IncludeFilter], ex: Option[ExcludeFilter]) {
  def mkString: String = {
    def getStrOrEmpty[A <: Filter](opt: Option[A]) = {
      opt.map(_.mkString).getOrElse("")
    }
    getStrOrEmpty[IncludeFilter](inc) + getStrOrEmpty[ExcludeFilter](ex)
  }
}

object FilterChain {
  def apply(inc: IncludeFilter, ex: ExcludeFilter) = new FilterChain(Some(inc), Some(ex))
  def apply(inc: IncludeFilter) = new FilterChain(Some(inc), None)
  def apply(ex: ExcludeFilter) = new FilterChain(None, Some(ex))
}

object EmptyFilter extends FilterChain(None, None)


abstract class Kind(val name: String)
case object Variable extends Kind("var") 
case object Container extends Kind("con")
case object Output extends Kind("output")
case object Source extends Kind("src")
case object Library extends Kind("lib")

object ClasspathConversions {
  implicit def pathToString(path: Path): String = path.toString
}

case class ClasspathEntry(kind: Kind, path: String, srcpath: Option[String], filter: FilterChain) {

  def mkString: String = mkString("")
  def mkString(sep: String): String = {
    sep + 
    "<classpathentry kind=\"" + kind.name + "\"" + 
    " path=\"" + path + "\"" + 
    writeSrcPath(srcpath) + 
    filter.mkString +
    " />"
  }  

  def writeSrcPath(srcpath: Option[String]): String = {
    srcpath match { 
      case Some(text) => " sourcepath=\"" +  text + "\""
      case None => ""
    }
  }
}

object ClasspathEntry {
  def apply(kind: Kind, path: String) = new ClasspathEntry(kind, path, None, EmptyFilter)
  def apply(kind: Kind, path: String, srcpath: String) = new ClasspathEntry(kind, path, Some(srcpath), EmptyFilter)
  def apply(kind: Kind, path: String, filter: FilterChain) = new ClasspathEntry(kind, path, None, filter)
  def apply(kind: Kind, path: String, srcpath: String, filter: FilterChain) = new ClasspathEntry(kind, path, Some(srcpath), filter)
}

class ClasspathFile(project: Project, log: Logger) {
  import ClasspathConversions._
  lazy val classpathFile: File = project.info.projectPath / ".classpath" asFile
  
  def writeFile: Option[String] = {
    
    def createOrReplaceWith(content: String): Option[String]= {
      
      FileUtilities.touch(classpathFile, log) match {
	case Some(error) =>
	  Some("Unable to write classpath file " + classpathFile + ": " + error)
	case None =>
	  FileUtilities.write(classpathFile, classpathContent.toString, forName("UTF-8"), log)
        }
    }

    def getDependencyEntries(path: Path): List[ClasspathEntry] = {
      import Path._
      val files = path.asFile.listFiles
      val jars = files.filter(file => file.isFile && file.getName.endsWith(".jar")).map(file => {
	val relativePath = Path.relativize(project.info.projectPath, file) match {
	  case Some(rPath) => rPath
	  case None => Path.fromFile(file)
	}
	ClasspathEntry(Library, relativePath)
      }).toList
      val subDirs = files.filter(file => file.isDirectory && file.getName != ".." && file.getName != ".").flatMap(file => getDependencyEntries(Path.fromFile(file))).toList
      jars ++ subDirs
    }

    val basicScalaPaths = project.asInstanceOf[BasicScalaPaths]
    val dependencies = basicScalaPaths.dependencyPath
    val managedDependencies = basicScalaPaths.managedDependencyPath

    val scalaContainer = "ch.epfl.lamp.sdt.launching.SCALA_CONTAINER"
    val javaContainer = "org.eclipse.jdt.launching.JRE_CONTAINER"

    val entries = getJavaPaths ++ getScalaPaths ++ getSbtJarForSbtProject ++ 
	      getDependencyEntries(dependencies) ++ getDependencyEntries(managedDependencies) ++ 
	      List(ClasspathEntry(Container, scalaContainer), 
		   ClasspathEntry(Container, javaContainer), 
		   ClasspathEntry(Output, project.asInstanceOf[MavenStyleScalaPaths].outputPath))
    
    lazy val classpathContent = 
      """<?xml version="1.0" encoding="UTF-8" ?>""" +
      "\n<classpath>" +
      ("" /: entries)(_ + _.mkString("\n")) +
      "\n</classpath>"
    createOrReplaceWith(classpathContent)
  }

  def getScalaPaths: List[ClasspathEntry] = {
    val paths = project.asInstanceOf[MavenStyleScalaPaths]
    var entries = List[ClasspathEntry]()
    entries = if(paths.mainScalaSourcePath.exists) { ClasspathEntry(Source, paths.mainScalaSourcePath, None, FilterChain(IncludeFilter("**/*.scala"))) :: entries } else entries
    if(paths.testScalaSourcePath.exists) { ClasspathEntry(Source, paths.testScalaSourcePath, None, FilterChain(IncludeFilter("**/*.scala"))) :: entries } else entries
  }

  def getJavaPaths: List[ClasspathEntry] = {
    val paths = project.asInstanceOf[MavenStyleScalaPaths]
    var entries = List[ClasspathEntry]()
    entries = if (paths.testJavaSourcePath.exists) {
      ClasspathEntry(Source, paths.testJavaSourcePath, None, FilterChain(IncludeFilter("**/*.java"))) :: entries 
    } else entries

    if (paths.mainJavaSourcePath.exists) { 
      ClasspathEntry(Source, paths.mainJavaSourcePath, None, FilterChain(IncludeFilter("**/*.java"))) :: entries 
    } else entries
  }

  def getSbtJarForSbtProject: List[ClasspathEntry] = {
    val scalaVersion = project.scalaVersion.get.get
    val sbtVersion = project.sbtVersion.get.get
    // TODO how to handle cross builds?
    val sbtLibPath = project.info.projectPath / "project" / "boot" / ("scala-" + scalaVersion) / ("sbt-" + sbtVersion) / ("sbt_" + scalaVersion + "-" + sbtVersion + ".jar")
    if(project.asInstanceOf[SbtEclipsifyPlugin].sbtDependency.value) List(ClasspathEntry(Library, sbtLibPath)) else Nil
  }
}

object ClasspathFile {
  def apply(project: Project, log: Logger) = new ClasspathFile(project, log)
}

