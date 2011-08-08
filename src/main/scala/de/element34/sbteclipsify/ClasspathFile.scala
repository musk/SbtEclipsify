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

import java.io.{ File, FileFilter => JFileFilter }

/**
 * Gathers the structural information for a .classpath file.
 * @param project The sbt project for which the .classpath file is created
 * @param log The logger from the sbt project
 */
case class ClasspathFile(ctx: ProjectCtx) {
	import CommandSupport.logger
	import Keys._
	import Utils._
	import Arguments._

	val log = logger(ctx.state)
	val extracted = Project.extract(ctx.state)
	val structure = extracted.structure

	def get[A] = setting[A](structure)_
	def eval[A] = evaluate[A](ctx.state, structure)_

	val name = get(ctx.ref, Keys.name, Compile).getOrElse("Unable to determine name")
	val baseDir = get(ctx.ref, Keys.baseDirectory, Compile)

	val ScalaLib = "scala-library.jar"
	val confs = List(Test, Provided, Runtime, Compile)

	def filter(key: SettingKey[FileFilter], conf: Seq[Configuration]): FileFilter = conf.map(c => { get(ctx.ref, key, c).getOrElse(NothingFilter) }).foldLeft(NothingFilter: FileFilter)(_ && _)
	def artifactKey(module: ModuleID, name: String, typ: String) = "%s %s %s".format(module, name, typ)
	def createClasspathEntry(kind: Kind, base: File, artifacts: Map[String, Option[File]], outputPath: Option[File] = None)(file: Attributed[File]): Option[ClasspathEntry] = {
		val exclude: FileFilter = filter(Keys.defaultExcludes, confs) && filter(Keys.sourceFilter, confs)
		val f = file.data
		log.debug("Processing %s".format(f))
		val result = f.getName == ScalaLib || exclude.accept(f)
		log.debug("%s filtered %b".format(f, result))
		if (!result) {
			val ak: Option[String] = file.metadata.get(Keys.artifact.key).flatMap(a => {
				file.metadata.get(Keys.moduleID.key).flatMap(m => {
					artifacts.get(artifactKey(m, a.name, "src")).flatMap(_.map(_.getAbsolutePath))
				})
			})
			Some(ClasspathEntry(kind, IO.relativize(base, f).getOrElse(f.getAbsolutePath), ak, outputPath.map(f => IO.relativize(base, f).getOrElse(f.getAbsolutePath))))
		} else
			None
	}

	def unpackResult[A](result: Result[Seq[A]], error: => String): Seq[A] = result.toEither match {
		case Right(files) =>
			files
		case Left(inc) =>
			log.error(error format (inc))
			Seq.empty[A]
	}

	def processSeq(seq: Seq[Attributed[File]], classpathBuilder: Attributed[File] => Option[ClasspathEntry]): Set[ClasspathEntry] =
		seq.map(classpathBuilder(_)).filter(_ match { case Some(_) => true; case None => false }).map(_.get).toSet

	def loadArtifactMap(ctx: ProjectCtx): Map[String, Option[File]] = if (ctx.args.contains(WITH_SOURCES)) {
		log.info("Retrieving sources...")
		val map = confs.map(c => {
			eval(ctx.ref, Keys.updateClassifiers, Compile).flatMap(_.toEither match {
				case Right(t) =>
					Some(t.configurations.flatMap(c => {
						c.modules.flatMap(m => {
							m.artifacts.map(_ match {
								case (Artifact(name, typ, extension, classifier, configurations, url, extraAttributes), f) =>
									(artifactKey(m.module, name, typ), Some(f))
								case _ =>
									("", None)
							})
						})
					}).toMap)
				case _ =>
					log.debug("Update classifieres failed!")
					None
			}).getOrElse(Map.empty)
		}).foldLeft(Map.empty[String, Option[File]])(_ ++ _)
		log.info("...done")
		map
	} else {
		log.debug("Skipping source retrieval!")
		Map.empty
	}

	def processProjectDependencies(baseDir: File): Set[ClasspathEntry] = {
		Project.getProject(ctx.ref, structure).map(p => {
			p.dependencies.map(d => {
				if (ctx.args.contains(JAR_DEPS)) {

						def createPkg[A](key: TaskKey[A], error: => String): Option[A] = eval(d.project, key, Compile).flatMap(_.toEither match {
							case Right(f) =>
								Some(f)
							case Left(f) =>
								log.error(error)
								None
						})

					log.debug("Creating package artifact for %s".format(d.project.project))
					val pkgBin = createPkg(Keys.packageBin, "Unable to create binary package for %s".format(d.project.project))

					val pkgSrc = if (ctx.args.contains(WITH_SOURCES)) {
						log.debug("Creating source package artifact for %s".format(d.project.project))
						createPkg(Keys.packageSrc, "Unable to create source package for %s".format(d.project.project))
					} else None

					pkgBin.map(f =>
						ClasspathEntry(Library,
							f.getAbsolutePath,
							pkgSrc.map(_.getAbsolutePath)))
				} else {
					get(d.project, Keys.name, Provided).map(pName => {
						ClasspathEntry(kind = Source, path = if (pName.startsWith("/")) pName else ("/" + pName), combineAccessRule = Some(false))
					})
				}
			}).filter(_ match { case Some(_) => true; case None => false }).map(_.get).toSet
		}).getOrElse(Set.empty[ClasspathEntry])
	}

	/**
	 * writes the .classpath file to the project root
	 * @return <code>Some(error)</code>, where error designates the error message to display, when an error occures else returns <code>None</code>
	 */
	def writeFile: Option[String] = {
		baseDir.map(bd => {

			val cpEntries: Set[ClasspathEntry] = {

				val artifactMap = loadArtifactMap(ctx)

					def procSrc(files: Seq[File], outputPath: Option[File] = None): Set[ClasspathEntry] = processSeq(files.map(f => {
						log.debug("Processing file %s".format(f))
						Attributed.blank(f)
					}), f => {
						if (!f.data.exists) {
							log.warn("""The source directory "%s" for project %s does not exist! Skipping entry creation.""".format(f.data, name))
							None
						} else createClasspathEntry(Source, bd, artifactMap, outputPath)(f)
					})

					def procLib(result: Result[Classpath]): Set[ClasspathEntry] =
						processSeq(unpackResult(result, "Unable to resolve dependencies! %s"), createClasspathEntry(Library, bd, artifactMap)_)

				val outputTest = get(ctx.ref, Keys.classDirectory, Test)
				val sources = get(ctx.ref, Keys.unmanagedSourceDirectories, Compile).map(procSrc(_)).getOrElse(Set.empty[ClasspathEntry])
				val sourcesTest = get(ctx.ref, Keys.unmanagedSourceDirectories, Test).map(procSrc(_, outputTest)).getOrElse(Set.empty[ClasspathEntry])
				val resources = get(ctx.ref, Keys.unmanagedResourceDirectories, Compile).map(procSrc(_)).getOrElse(Set.empty[ClasspathEntry])
				val resourcesTest = get(ctx.ref, Keys.unmanagedResourceDirectories, Test).map(procSrc(_, outputTest)).getOrElse(Set.empty[ClasspathEntry])

				val compileJars = eval(ctx.ref, Keys.unmanagedClasspath, Compile).map(procLib(_)).getOrElse(Set.empty[ClasspathEntry])
				val testJars = eval(ctx.ref, Keys.unmanagedClasspath, Test).map(procLib(_)).getOrElse(Set.empty[ClasspathEntry])
				val runtimeJars = eval(ctx.ref, Keys.unmanagedClasspath, Runtime).map(procLib(_)).getOrElse(Set.empty[ClasspathEntry])
				val providedJars = eval(ctx.ref, Keys.unmanagedClasspath, Provided).map(procLib(_)).getOrElse(Set.empty[ClasspathEntry])

				val clibs = eval(ctx.ref, Keys.externalDependencyClasspath, Compile).map(procLib(_)).getOrElse(Set.empty[ClasspathEntry])
				val tlibs = eval(ctx.ref, Keys.externalDependencyClasspath, Test).map(procLib(_)).getOrElse(Set.empty[ClasspathEntry])
				val rlibs = eval(ctx.ref, Keys.externalDependencyClasspath, Runtime).map(procLib(_)).getOrElse(Set.empty[ClasspathEntry])
				val plibs = eval(ctx.ref, Keys.externalDependencyClasspath, Provided).map(procLib(_)).getOrElse(Set.empty[ClasspathEntry])

				val output = get(ctx.ref, Keys.classDirectory, Compile).map(cd => {
					Set(ClasspathEntry(Output, IO.relativize(bd, cd).getOrElse(cd.getAbsolutePath)))
				}).getOrElse(Set.empty[ClasspathEntry])

				val classpath = clibs ++
					tlibs ++
					rlibs ++
					plibs ++
					compileJars ++
					testJars ++
					runtimeJars ++
					providedJars ++
					sources ++
					sourcesTest ++
					resources ++
					resourcesTest ++
					processProjectDependencies(ctx.projectBase) ++
					output ++
					nature(ctx.ref, structure, log).container.map(ClasspathEntry(Container, _))

				log.debug("Classpath entries:%n%s".format(
					classpath.map(n => {
						"%s%n  lib=%s,%n  src=%s%n".format(n.kind, n.path, n.srcPath.getOrElse("<none>"))
					}).mkString("\n")))
				classpath
			}

			lazy val classpathContent = <classpath>
				{ (NodeSeq.Empty /: cpEntries)(_ ++ _.toNodeSeq) }
			</classpath>

			val classpathFile = (Path(bd) / ".classpath").getAbsolutePath
			try {
				XML.save(classpathFile, classpathContent, "utf-8", true)
				None
			} catch {
				case e => Some("Error writing file %s:%n%s".format(classpathFile, e))
			}
		}) getOrElse Some("Unable to resolve base directory for project %s" format name)
	}
}

