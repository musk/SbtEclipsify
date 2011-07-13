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

/**
 * Gathers the structural information for a .classpath file.
 * @param project The sbt project for which the .classpath file is created
 * @param log The logger from the sbt project
 *
 * TODO add support for filters
 */
case class ClasspathFile(ref: ProjectRef, state: State) {
	import CommandSupport.logger
	import Keys._
	import Utils._

	val log = logger(state)
	val extracted = Project.extract(state)
	implicit val structure = extracted.structure
	implicit val implicitState = state
	val name = setting(ref, Keys.name, Compile).getOrElse("Unable to determine name")
	val baseDir = setting(ref, Keys.baseDirectory, Compile)

	val ScalaLib = "scala-library.jar"

	/**
	 * writes the .classpath file to the project root
	 * @return <code>Some(error)</code>, where error designates the error message to display, when an error occures else returns <code>None</code>
	 */
	def writeFile: Option[String] = {
		baseDir.map(projectBase => {

			val cpEntries: Set[ClasspathEntry] = {

				val compileExcludes = setting(ref, Keys.defaultExcludes, Compile).getOrElse(NothingFilter)
				val testExcludes = setting(ref, Keys.defaultExcludes, Test).getOrElse(NothingFilter)
				val runtimeExcludes = setting(ref, Keys.defaultExcludes, Runtime).getOrElse(NothingFilter)
				val provExcludes = setting(ref, Keys.defaultExcludes, Provided).getOrElse(NothingFilter)
				val exclude: FileFilter = compileExcludes && testExcludes && runtimeExcludes && provExcludes

					//					def processSrc(sources: Seq[Classpath]): Set[ClasspathEntry] = sources.map(file => {
					//						log.debug("Processing %s for classpathentry!".format(file))
					//						ClasspathEntry(Source, IO.relativize(projectBase, file).getOrElse(file.getAbsolutePath))
					//					}).toSet
					implicit def convAF2F(file: Attributed[File]): File = file.data
					implicit def noConv(file: File): File = file

					def createClasspathEntry[A](kind: Kind, files: Seq[A])(implicit conv: A => File) = files.map(conv).filterNot(f => {
						val result = f.getName == ScalaLib || exclude.accept(f)
						log.debug("%s filtered %b".format(f, result))
						result
					}).map(f => {
						ClasspathEntry(kind, IO.relativize(projectBase, f).getOrElse(f.getAbsolutePath))
					}).toSet

					def processResult[A](kind: Kind)(res: Result[Seq[A]])(implicit conv: A => File): Set[ClasspathEntry] = res.toEither match {
						case Right(files) =>
							createClasspathEntry(kind, files)(conv)
						case Left(inc) =>
							log.error("Unable to resolve compile dependencies! %s".format(inc))
							Set.empty[ClasspathEntry]
					}

					def evaluate[A](ref: ProjectRef, key: TaskKey[A], config: Configuration)(implicit state: State, structure: Load.BuildStructure) =
						EvaluateTask.evaluateTask(structure, key in config, state, ref, false, EvaluateTask.SystemProcessors)

				val procLib = processResult[Attributed[File]](Library) _
				val procSrc = processResult[File](Source) _

				// TODO need to find a way to process files as attributed files
				val sources = evaluate(ref, Keys.sources, Compile).map(procSrc(_)).getOrElse(Set.empty[ClasspathEntry])
				val resources = evaluate(ref, Keys.resources, Compile).map(procSrc(_)).getOrElse(Set.empty[ClasspathEntry])

				val compileJars = evaluate(ref, Keys.unmanagedClasspath, Compile).map(procLib(_)).getOrElse(Set.empty[ClasspathEntry])
				val testJars = evaluate(ref, Keys.unmanagedClasspath, Test).map(procLib(_)).getOrElse(Set.empty[ClasspathEntry])
				val runtimeJars = evaluate(ref, Keys.unmanagedClasspath, Runtime).map(procLib(_)).getOrElse(Set.empty[ClasspathEntry])
				val providedJars = evaluate(ref, Keys.unmanagedClasspath, Provided).map(procLib(_)).getOrElse(Set.empty[ClasspathEntry])

				val clibs = evaluate(ref, Keys.externalDependencyClasspath, Compile).map(procLib(_)).getOrElse(Set.empty[ClasspathEntry])
				val tlibs = evaluate(ref, Keys.externalDependencyClasspath, Test).map(procLib(_)).getOrElse(Set.empty[ClasspathEntry])
				val rlibs = evaluate(ref, Keys.externalDependencyClasspath, Runtime).map(procLib(_)).getOrElse(Set.empty[ClasspathEntry])
				val plibs = evaluate(ref, Keys.externalDependencyClasspath, Provided).map(procLib(_)).getOrElse(Set.empty[ClasspathEntry])

				val classpath = clibs ++ tlibs ++ rlibs ++ plibs ++ compileJars ++ testJars ++ runtimeJars ++ providedJars ++ sources ++ resources
				log.debug("Finding classpathentries %s".format(classpath.map(n => n.path + " - " + n.kind).mkString(",")))
				classpath
			}

			lazy val classpathContent = <classpath>
				{ (NodeSeq.Empty /: cpEntries)(_ ++ _.toNodeSeq) }
			</classpath>

			val classpathFile = (Path(projectBase) / ".classpath").getAbsolutePath
			try {
				XML.save(classpathFile, classpathContent, "utf-8", true)
				None
			} catch {
				case e => Some("Error writing file %s:%n%s".format(classpathFile, e))
			}
		}) getOrElse Some("Unable to resolve base directory for project %s" format name)
	}

	//	def classpaths(): List[ClasspathEntry] = {
	//		project match {
	//			case p: BasicScalaProject => {
	//				val pf = (p.unmanagedClasspath
	//					+++ p.managedClasspath(p.config("compile"))
	//					+++ p.managedClasspath(p.config("test"))
	//					+++ p.managedClasspath(p.config("runtime")) // to be able to do "Run As ..."
	//					+++ p.jarsOfProjectDependencies)
	//				pf.getPaths.toList.map { x =>
	//					ClasspathEntry(Library, x)
	//				}
	//			}
	//			case p: UnmanagedClasspathProject => {
	//				p.unmanagedClasspath.getPaths.toList.map { x =>
	//					ClasspathEntry(Library, x)
	//				}
	//			}
	//			case p: BasicScalaPaths => {
	//				getDependencyEntries(p.dependencyPath)
	//			}
	//			case _ => Nil
	//		}
	//	}

	//	def getReferencedProjects(projects: List[Project]): List[ClasspathEntry] = {
	//		projects.map { proj =>
	//			ClasspathEntry(Source, "/" + proj.name, List(("combineaccessrules", "false")))
	//		}
	//	}
	//
	//	def getReferencedProjectsDependencies(projects: List[Project]): List[ClasspathEntry] = {
	//		projects.foldLeft(List[ClasspathEntry]()) { (list, proj) =>
	//			val basicScalaPaths = proj.asInstanceOf[BasicScalaPaths]
	//			val dependencies = basicScalaPaths.dependencyPath
	//			val managedDependencies = basicScalaPaths.managedDependencyPath
	//
	//			list ++
	//				getReferencedProjectDependencyEntries(proj.name, proj.info.projectPath, dependencies) ++
	//				getReferencedProjectDependencyEntries(proj.name, proj.info.projectPath, managedDependencies)
	//		}
	//	}
	//	def getReferencedProjectDependencyEntries(extProjName: String, extProjPath: Path, basePath: Path): List[ClasspathEntry] = {
	//		import Path._
	//		val exclude: List[PathFinder] = constructPathFinder(basePath, srcPatterns, str => GlobFilter("*" + str))
	//		val baseFinder: PathFinder = basePath ** GlobFilter("*.jar")
	//		val finder: PathFinder = exclude.foldLeft(baseFinder)(_ --- _)
	//
	//		val jarPaths = finder.get.flatMap(Path.relativize(extProjPath, _)).toList
	//		jarPaths.map { path =>
	//			val eclipsePath = "/" + extProjName + "/" + path.relativePath
	//			ClasspathEntry(Library, eclipsePath, Some(eclipsePath))
	//		}
	//	}
	//
	//	/**
	//	 * @return <code>List[ClasspathEntry]</code> containing entries for each jar contained in path.
	//	 */
	//	def getDependencyEntries(basePath: Path): List[ClasspathEntry] = {
	//		import Path._
	//
	//		val exclude: List[PathFinder] = constructPathFinder(basePath, srcPatterns, str => GlobFilter("*" + str))
	//		val baseFinder: PathFinder = basePath ** GlobFilter("*.jar")
	//		val finder: PathFinder = exclude.foldLeft(baseFinder)(_ --- _)
	//
	//		val jarPaths: List[Path] = finder.get.flatMap(Path.relativize(project.info.projectPath, _)).toList
	//		jarPaths.map(path => ClasspathEntry(Library, path.relativePath, findSource(basePath, path)))
	//	}
	//
	//	private def findSource(basePath: Path, jar: Path): Option[String] = {
	//		import sbt.Project._
	//		val JarEx = """.*/([^/]*)\.jar""".r
	//		jar.toString match {
	//			case JarEx(name) => {
	//				val finders: List[PathFinder] = constructPathFinder(basePath, srcPatterns, str => new ExactFilter(name + str))
	//				val seq = finders.foldLeft(Path.emptyPathFinder)(_ +++ _).get.toSeq
	//				seq.firstOption.map(_.toString)
	//			}
	//			case _ => None
	//		}
	//	}
	//
	//	private def constructPathFinder(basePath: Path, list: List[String], conv: String => FileFilter): List[PathFinder] = {
	//		list.map(str => basePath ** conv(str))
	//	}
	//
	//	/**
	//	 * @return <code>List[ClasspathEntry]</code> with entries for the project build directory and the project plugin directory
	//	 */
	//	def getProjectPath: List[ClasspathEntry] = {
	//		val entries: List[ClasspathEntry] = if (project.includeProject && project.info.builderProjectPath.exists) {
	//			ClasspathEntry(Source, project.info.builderProjectPath, FilterChain(IncludeFilter("**/*.scala"))) :: Nil
	//		} else Nil
	//
	//		if (project.includePlugin && project.info.pluginsPath.exists) {
	//			ClasspathEntry(Source, project.info.pluginsPath, FilterChain(IncludeFilter("**/*.scala"))) :: entries
	//		} else entries
	//	}
	//
	//	/**
	//	 * @return <code>List[ClasspathEntry]</code> with entries for the main source and main test source path.
	//	 */
	//	def getScalaPaths: List[ClasspathEntry] = {
	//		import ClasspathConversions._
	//		val paths = project.asInstanceOf[MavenStyleScalaPaths]
	//		val entries: List[ClasspathEntry] = if (paths.mainScalaSourcePath.exists) {
	//			ClasspathEntry(Source, paths.mainScalaSourcePath.relativePath, FilterChain(IncludeFilter("**/*.scala"), ExcludeFilter("**/.svn/|**/CVS/"))) :: Nil
	//		} else Nil
	//
	//		if (paths.testScalaSourcePath.exists) {
	//			ClasspathEntry(Source, paths.testScalaSourcePath.relativePath, paths.testCompilePath.relativePath, FilterChain(IncludeFilter("**/*.scala"), ExcludeFilter("**/.svn/|**/CVS/"))) :: entries
	//		} else entries
	//	}
	//
	//	/**
	//	 * @return <code>List[ClasspathEntry]</code> for main java source and main java test source path
	//	 */
	//	def getJavaPaths: List[ClasspathEntry] = {
	//		import ClasspathConversions._
	//		val paths = project.asInstanceOf[MavenStyleScalaPaths]
	//		val entries = new ListBuffer[ClasspathEntry]()
	//		if (paths.mainJavaSourcePath.exists) {
	//			entries + ClasspathEntry(Source, paths.mainJavaSourcePath.projectRelativePath, FilterChain(IncludeFilter("**/*.java")))
	//		}
	//		if (paths.mainResourcesPath.exists) {
	//			entries + ClasspathEntry(Source, paths.mainResourcesPath.projectRelativePath)
	//		}
	//		if (paths.testJavaSourcePath.exists) {
	//			entries + ClasspathEntry(Source, paths.testJavaSourcePath.projectRelativePath, paths.testCompilePath.projectRelativePath, FilterChain(IncludeFilter("**/*.java")))
	//		}
	//		if (paths.testResourcesPath.exists) {
	//			entries + ClasspathEntry(Source, paths.testResourcesPath.projectRelativePath, paths.testCompilePath.projectRelativePath, EmptyFilter)
	//		}
	//		return entries.toList
	//	}
	//
	//	/**
	//	 * @return <code>List[ClasspathEntry]</code> for main java source and main java test source path
	//	 */
	//	def getResourcesPaths: List[ClasspathEntry] = {
	//		val paths = project.asInstanceOf[MavenStyleScalaPaths]
	//		var entries: List[ClasspathEntry] = Nil
	//		if (paths.mainResourcesPath.exists) {
	//			entries = ClasspathEntry(Source, paths.mainResourcesPath.projectRelativePath) :: entries
	//		}
	//		if (paths.testResourcesPath.exists) {
	//			entries = ClasspathEntry(Source, paths.testResourcesPath.projectRelativePath, paths.testCompilePath.projectRelativePath, EmptyFilter) :: entries
	//		}
	//		entries
	//	}
	//
	//	/**
	//	 * @return <code>List[ClasspathEntry]</code> for sbt jar
	//	 */
	//	def getSbtJarForSbtProject: List[ClasspathEntry] = {
	//		//val plugin = project.asInstanceOf[Eclipsify]
	//		if (project.sbtDependency || project.includeProject || project.includePlugin) {
	//			val scalaVersion = project.buildScalaVersion
	//			val sbtVersion = project.sbtVersion.get.get
	//			// TODO how to handle cross builds?
	//			val sbtJar = "sbt_" + scalaVersion + "-" + sbtVersion + ".jar"
	//			val foundPaths = project.info.projectPath / "project" / "boot" ** new ExactFilter(sbtJar) get
	//			val entries: List[ClasspathEntry] = foundPaths.map(ClasspathEntry(Library, _)).toList
	//			entries
	//		} else Nil
	//	}
	//
	//	def getPluginEntries: List[ClasspathEntry] = {
	//		//		val plugin = project.asInstanceOf[Eclipsify]
	//		if (project.pluginProject)
	//			List(ClasspathEntry(Container, depPluginsContainer))
	//		else Nil
	//	}
}

