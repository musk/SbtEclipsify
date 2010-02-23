/**
 * Copyright (c) 2010, Stefan Langer
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Element34 nor the names of its contributors may
 *       be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS ROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package de.element34.sbteclipsify

import scala.xml._
import sbt._

import java.io.File
import java.nio.charset.Charset._

abstract class Filter(pattern: String) {
  def mkString = "=\"" + pattern + "\""
}

/**
 * Defines a include pattern for a classpath entry. <br />
 * e.g.: <code>&lt;classpathentry kind="src" path="src/" including="*.scala" /&gt;</code>
 */
case class IncludeFilter(pattern: String) extends Filter(pattern) {
  override def mkString = " including" + super.mkString
}
/**
 * Defines a exclude pattern for a classpath entry. <br />
 * e.g.: <code>&lt;classpathentry kind="src" path="src/" excluding="*.scala" /&gt;</code>
 */
case class ExcludeFilter(pattern: String) extends Filter(pattern) {
  override def mkString = " excluding" + super.mkString
}
/**
 * Combines <code>IncludeFilter</code> and <code>ExcludeFilter</code> for use in a <code>ClasspathEntry</code>
 */
case class FilterChain(inc: Option[IncludeFilter], ex: Option[ExcludeFilter]) {
  /**
   * Generates the actual markup for a classpathentry
   */
  def mkString: String = {
    def getStrOrEmpty[A <: Filter](opt: Option[A]) = {
      opt.map(_.mkString).getOrElse("")
    }
    getStrOrEmpty[IncludeFilter](inc) + getStrOrEmpty[ExcludeFilter](ex)
  }
}

/**
 * Companion object for <code>FilterChain</code> to provide convenience method for their creation.
 */
object FilterChain {
  def apply(inc: IncludeFilter, ex: ExcludeFilter) = new FilterChain(Some(inc), Some(ex))
  def apply(inc: IncludeFilter) = new FilterChain(Some(inc), None)
  def apply(ex: ExcludeFilter) = new FilterChain(None, Some(ex))
}

/**
 * Special type designating an empty <code>FilterChain</code>
 */
object EmptyFilter extends FilterChain(None, None)

abstract class Kind(val name: String)
/** defines the variable kind("var") for a classpathentry*/
case object Variable extends Kind("var")
/** defines the container kind ("con") for a classpathentry */
case object Container extends Kind("con")
/** defines the output kind ("output") for a classpathentry */
case object Output extends Kind("output")
/** defines the source kind ("src") for a classpathentry */
case object Source extends Kind("src")
/** defines the library kind ("lib") for a classpathentry */
case object Library extends Kind("lib")

object ClasspathConversions {
	/** implicit conversion from a path to a string */
	implicit def pathToString(path: Path): String = path.toString
}

/**
 * Defines a classpathentry in a .classpath file.
 * Each entry has a kind (either src, output, lib, var or con),
 * a path designating its location, a optional source path and
 * a include and exlucde filter as well as arbitrary attributes
 * that specify further information for the classpathentry.
 *
 * @see the eclipse documentatin for further information about classpathentries
 */
case class ClasspathEntry(kind: Kind, path: String, srcpath: Option[String], filter: FilterChain, attributes: List[Tuple2[String, String]]) {
  /** @see mkString(sep: String) */
  def mkString: String = mkString("")
  /**
   * converts this <code>ClasspathEntry</code > into a xml string representation
   * @param sep Defines the leading separater <code>String</code> prepended to each classpathentry
   */
  def mkString(sep: String): String = {
    sep +
    "<classpathentry kind=\"" + kind.name + "\"" +
    " path=\"" + path + "\"" +
    writeSrcPath(srcpath) +
    filter.mkString + (
	    if(attributes.isEmpty)
	    	" />"
	    else {
	    	def mkAttribute(item: Tuple2[String, String]) = {
	    	  "<attribute name=\"" + item._1 + "\" value=\"" + item._2 + "\" />"
	    	}
	    	val attrstr = ("" /: attributes.map(mkAttribute))(_ + _)
	    	">\n<attributes>\n"+  attrstr  + "\n</attributes>\n</classpathentry>"
	    }
    )
  }
  /** returns the sourcepath as a string when specified */
  def writeSrcPath(srcpath: Option[String]): String = {
    srcpath match {
      case Some(text) => " sourcepath=\"" +  text + "\""
      case None => ""
    }
  }
}

/**
 * Factory providing convenience methods for creating <code>ClasspathEntry</code>
 */
object ClasspathEntry {
  def apply(kind: Kind, path: String) = new ClasspathEntry(kind, path, None, EmptyFilter, Nil)
  def apply(kind: Kind, path: String, srcpath: Option[String]) = new ClasspathEntry(kind, path, srcpath, EmptyFilter, Nil)
  def apply(kind: Kind, path: String, srcpath: String) = new ClasspathEntry(kind, path, Some(srcpath), EmptyFilter, Nil)
  def apply(kind: Kind, path: String, filter: FilterChain) = new ClasspathEntry(kind, path, None, filter, Nil)
  def apply(kind: Kind, path: String, srcpath: String, filter: FilterChain) = new ClasspathEntry(kind, path, Some(srcpath), filter, Nil)
  def apply(kind: Kind, path: String, attributes: List[Tuple2[String, String]]) = new ClasspathEntry(kind, path, None, EmptyFilter, attributes)
  def apply(kind: Kind, path: String, srcpath: String, attributes: List[Tuple2[String, String]]) = new ClasspathEntry(kind, path, Some(srcpath), EmptyFilter, attributes)
  def apply(kind: Kind, path: String, filter: FilterChain, attributes: List[Tuple2[String, String]]) = new ClasspathEntry(kind, path, None, filter, attributes)
  def apply(kind: Kind, path: String, srcpath: String, filter: FilterChain, attributes: List[Tuple2[String, String]]) = new ClasspathEntry(kind, path, Some(srcpath), filter, attributes)
}

/**
 * Gathers the structural information for a .classpath file.
 * @param project The sbt project for which the .classpath file is created
 * @param log The logger from the sbt project
 */
class ClasspathFile(project: Project, log: Logger) {
	import ClasspathConversions._
    lazy val classpathFile: File = project.info.projectPath / ".classpath" asFile

    /**
     * writes the .classpath file to the project root
     * @return <code>Some(error)</code>, where error designates the error message to display, when an error occures else returns <code>None</code>
     */
    def writeFile: Option[String] = {
    	val basicScalaPaths = project.asInstanceOf[BasicScalaPaths]
    	val dependencies = basicScalaPaths.dependencyPath
    	val managedDependencies = basicScalaPaths.managedDependencyPath

    	val scalaContainer = "ch.epfl.lamp.sdt.launching.SCALA_CONTAINER"
    	val javaContainer = "org.eclipse.jdt.launching.JRE_CONTAINER"

    	val entries = getJavaPaths ++ getScalaPaths ++ getProjectPath ++ getSbtJarForSbtProject ++
	      			  getDependencyEntries(dependencies) ++ getDependencyEntries(managedDependencies) ++
	      			  List(ClasspathEntry(Container, scalaContainer),
	      			  ClasspathEntry(Container, javaContainer),
	      			  ClasspathEntry(Output, project.asInstanceOf[MavenStyleScalaPaths].mainCompilePath))

	    lazy val classpathContent = """<?xml version="1.0" encoding="UTF-8" ?>""" +
	    	"\n<classpath>" +
	    	("" /: entries)(_ + _.mkString("\n")) +
	    	"\n</classpath>"
	    createOrReplaceWith(classpathContent)
  	}

	/**
	 * replaces the current content of the .classpath file
	 * @return <code>Some(error)</code> when error occurs else returns <code>None</code>
     */
	def createOrReplaceWith(content: String): Option[String]= {
		FileUtilities.touch(classpathFile, log) match {
			case Some(error) =>
				Some("Unable to write classpath file " + classpathFile + ": " + error)
			case None =>
				FileUtilities.write(classpathFile, content, forName("UTF-8"), log)
		}
	}

	/**
     * @return <code>List[ClasspathEntry]</code> containing entries for each jar contained in path.
     */
	def getDependencyEntries(basePath: Path): List[ClasspathEntry] = {
		import Path._

		val finder: PathFinder = basePath ** GlobFilter("*.jar") --- basePath ** GlobFilter("*-sources.jar")
		val jarPaths: List[Path] = finder.get.flatMap(Path.relativize(project.info.projectPath, _)).toList
		jarPaths.map(path => ClasspathEntry(Library, path.relativePath, findSource(basePath, path)))
	}

	def findSource(basePath: Path, jar: Path): Option[String] = {
		import sbt.Project._
		val JarEx = """.*/([^/]*)\.jar""".r
		jar.toString match {
			case JarEx(name) => {
				val jarName = name + "-sources.jar"
				log.info(jarName)
				val finder: PathFinder = basePath ** new ExactFilter(jarName)
				val seq = finder.get.toSeq
				seq.firstOption.map(_.toString)
			}
			case _ => None
		}
	}

	/**
     * @return <code>List[ClasspathEntry]</code> with entries for the project build directory and the project plugin directory
     */
  	def getProjectPath: List[ClasspathEntry] = {
	    val plugin = project.asInstanceOf[SbtEclipsifyPlugin]
	    val entries: List[ClasspathEntry] = if(plugin.includeProject.value && project.info.builderProjectPath.exists) {
	    	ClasspathEntry(Source, project.info.builderProjectPath, FilterChain(IncludeFilter("**/*.scala"))) :: Nil
	    } else Nil

	    if(plugin.includePlugin.value && project.info.pluginsPath.exists) {
	    	ClasspathEntry(Source, project.info.pluginsPath, FilterChain(IncludeFilter("**/*.scala"))) :: entries
	    } else entries
	}

    /**
     * @return <code>List[ClasspathEntry]</code> with entries for the main source and main test source path.
     */
	def getScalaPaths: List[ClasspathEntry] = {
	    val paths = project.asInstanceOf[MavenStyleScalaPaths]
	    val entries: List[ClasspathEntry] = if(paths.mainScalaSourcePath.exists) {
	    	ClasspathEntry(Source, paths.mainScalaSourcePath, FilterChain(IncludeFilter("**/*.scala"))) :: Nil
	    } else Nil

	    if(paths.testScalaSourcePath.exists) {
	    	ClasspathEntry(Source, paths.testScalaSourcePath, FilterChain(IncludeFilter("**/*.scala"))) :: entries
	    } else entries
	}

    /**
     * @return <code>List[ClasspathEntry]</code> for main java source and main java test source path
     */
	def getJavaPaths: List[ClasspathEntry] = {
	    val paths = project.asInstanceOf[MavenStyleScalaPaths]
	    val entries: List[ClasspathEntry] = if (paths.testJavaSourcePath.exists) {
	    	ClasspathEntry(Source, paths.testJavaSourcePath, FilterChain(IncludeFilter("**/*.java"))) :: Nil
	    } else Nil

	    if (paths.mainJavaSourcePath.exists) {
	    	ClasspathEntry(Source, paths.mainJavaSourcePath, FilterChain(IncludeFilter("**/*.java"))) :: entries
	    } else entries
	}


	/**
     * @return <code>List[ClasspathEntry]</code> for sbt jar
     */
	def getSbtJarForSbtProject: List[ClasspathEntry] = {
	    val plugin = project.asInstanceOf[SbtEclipsifyPlugin]
	    if(plugin.sbtDependency.value || plugin.includeProject.value || plugin.includePlugin.value) {
			val scalaVersion = project.buildScalaVersion
		    val sbtVersion = project.sbtVersion.get.get
		    // TODO how to handle cross builds?
		    val sbtJar = "sbt_" + scalaVersion + "-" + sbtVersion + ".jar"
		    val foundPaths = project.info.projectPath / "project" / "boot" ** new ExactFilter(sbtJar) get
		    val entries: List[ClasspathEntry] = foundPaths.map(ClasspathEntry(Library, _)).toList
		    entries
		}
	    else Nil
	}
}

/**
 * Factory to for creating ClasspathFile instances
 */
object ClasspathFile {

	def apply(project: Project, log: Logger) = new ClasspathFile(project, log)
}

