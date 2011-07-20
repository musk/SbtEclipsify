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
	def createClasspathEntry(kind: Kind, base: File, artifacts: Map[String, Option[File]])(file: Attributed[File]): Option[ClasspathEntry] = {
		val exclude: FileFilter = filter(Keys.defaultExcludes, confs) && filter(Keys.sourceFilter, confs)
		val f = file.data
		val result = !(f.getName == ScalaLib || exclude.accept(f))
		log.debug("%s filtered %b".format(f, result))
		if (result) {
			val ak: Option[String] = file.metadata.get(Keys.artifact.key).flatMap(a => {
				file.metadata.get(Keys.moduleID.key).flatMap(m => {
					artifacts.get(artifactKey(m, a.name, "src")).flatMap(_.map(_.getAbsolutePath))
				})
			})
			// TODO add outputdirecotry classesDirectory testClassesDirectory
			Some(ClasspathEntry(kind, IO.relativize(base, f).getOrElse(f.getAbsolutePath), ak))
		} else
			None
	}

	def processResult[A](classpathBuilder: Attributed[File] => Option[ClasspathEntry])(conv: A => Attributed[File])(res: Result[Seq[A]]): Set[ClasspathEntry] = res.toEither match {
		case Right(files) =>
			files.map(f => classpathBuilder(conv(f))).filter(_ match { case Some(_) => true; case None => false }).map(_.get).toSet
		case Left(inc) =>
			log.error("Unable to resolve dependencies! %s".format(inc))
			Set.empty[ClasspathEntry]
	}

	def processProjectDependencies(baseDir: File): Set[ClasspathEntry] = {
		Project.getProject(ctx.ref, structure).map(p => {
			p.dependencies.map(d => {
				Project.getProject(d.project, structure).map(x => {
					val path = IO.relativize(baseDir, x.base).getOrElse(x.base.getAbsolutePath)
					ClasspathEntry(Source, "/" + path, false)
				})
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

				val artifactMap: Map[String, Option[File]] = eval(ctx.ref, Keys.updateClassifiers, Compile).flatMap(_.toEither match {
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
				}).getOrElse(Map.empty[String, Option[File]])

				val procLib = processResult[Attributed[File]](createClasspathEntry(Library, bd, artifactMap)_)(f => f) _
				val procSrc = processResult[File](createClasspathEntry(Source, bd, artifactMap)_)(f => Attributed.blank(f))_

				val sources = eval(ctx.ref, Keys.sources, Compile).map(procSrc(_)).getOrElse(Set.empty[ClasspathEntry])
				val resources = eval(ctx.ref, Keys.resources, Compile).map(procSrc(_)).getOrElse(Set.empty[ClasspathEntry])

				val compileJars = eval(ctx.ref, Keys.unmanagedClasspath, Compile).map(procLib(_)).getOrElse(Set.empty[ClasspathEntry])
				val testJars = eval(ctx.ref, Keys.unmanagedClasspath, Test).map(procLib(_)).getOrElse(Set.empty[ClasspathEntry])
				val runtimeJars = eval(ctx.ref, Keys.unmanagedClasspath, Runtime).map(procLib(_)).getOrElse(Set.empty[ClasspathEntry])
				val providedJars = eval(ctx.ref, Keys.unmanagedClasspath, Provided).map(procLib(_)).getOrElse(Set.empty[ClasspathEntry])

				val clibs = eval(ctx.ref, Keys.externalDependencyClasspath, Compile).map(procLib(_)).getOrElse(Set.empty[ClasspathEntry])
				val tlibs = eval(ctx.ref, Keys.externalDependencyClasspath, Test).map(procLib(_)).getOrElse(Set.empty[ClasspathEntry])
				val rlibs = eval(ctx.ref, Keys.externalDependencyClasspath, Runtime).map(procLib(_)).getOrElse(Set.empty[ClasspathEntry])
				val plibs = eval(ctx.ref, Keys.externalDependencyClasspath, Provided).map(procLib(_)).getOrElse(Set.empty[ClasspathEntry])

				val classpath = clibs ++
					tlibs ++
					rlibs ++
					plibs ++
					compileJars ++
					testJars ++
					runtimeJars ++
					providedJars ++
					sources ++
					resources ++
					processProjectDependencies(ctx.projectBase)
				log.debug("Classpath entries:%n%s".format(classpath.map(n => n.path + " ---> " + n.kind).mkString("\t", "\n\t", "")))
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

