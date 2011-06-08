/**
 * Copyright (c) 2010, Stefan Langer and others
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

import sbt._

/**
 * Defines the plugin with the "eclipse" task for sbt
 */
object Eclipsify extends Plugin {
	import CommandSupport.logger
	import Keys._

	override lazy val settings = Seq(commands += eclipse)

	val description = SettingKey[String]("description")
	val nature = SettingKey[ProjectNature]("nature")

	lazy val eclipse: Command = Command.command("eclipse") { state =>
		val log = logger(state)

		val currProject = Project.current(state)
		log.info("Current project: %s" format (currProject))

		val extracted = Project.extract(state)
		val structure = extracted.structure

		for (ref <- structure.allProjectRefs) {

			val name = Keys.name in (ref, Compile) get structure.data
			ProjectFile(ref, state).writeFile match {
				case None => log.info("written .project for %s" format name)
				case Some(err) => log.info("unable to write .project for %s due to %s".format(name, err))
			}
			ClasspathFile(ref, state).writeFile match {
				case None => log.info("written .classpath for %s" format name)
				case Some(err) => log.info("unable to write .classpath for %s due to %s".format(name, err))
			}

			//			val depProject = Project.getProject(ref, structure)
			//			depProject.foreach(dp => {
			//				val name = ref.project
			//				log.info("Dependencies from %s: %s".format(name ,dp.dependencies))
			//				log.info("Uses from %s: %s".format(name,dp.uses))
			//				log.info("Referenced from %s: %s".format(name,dp.referenced))
			//			})
			//
			//
			//			val baseDir = (Keys.baseDirectory in (ref, Compile) get structure.data).getOrElse("<Base Dir not available>")
			//			log.info("BaseDir: %s" format baseDir)
			//
			//			val managedSources = (Keys.managedSourceDirectories in (ref, Compile) get structure.data).getOrElse("<No managed sources available>")
			//			log.info("Managed sources: %s" format managedSources)
			//
			//			val unmanagedSources = (Keys.unmanagedSourceDirectories in (ref, Compile) get structure.data).getOrElse("<No unmanaged sources available>")
			//			log.info("Unmanaged sources: %s" format unmanagedSources)
			//
			//			val managedRes = (Keys.managedResourceDirectories in (ref, Compile) get structure.data) getOrElse "<No managed resources>"
			//			log.info("Managed resources: %s" format managedRes)
			//
			//			val unmanagedRes = (Keys.unmanagedResourceDirectories in (ref, Compile) get structure.data) getOrElse "<No unmanaged resources>"
			//			log.info("Unmanaged resources: %s" format unmanagedRes)
			//
			//			val extDep = EvaluateTask.evaluateTask(structure, Keys.externalDependencyClasspath in Compile, state, ref, false, EvaluateTask.SystemProcessors)
			//			log.info("External Dependencies: %s" format extDep)
			//
			//			val srcFilter = (Keys.sourceFilter in (ref, Compile) get structure.data).getOrElse("<Source filter not available>")
			//			log.info("Source Filter: %s" format srcFilter)
			//
			//			val unmanagedJars = EvaluateTask.evaluateTask(structure, Keys.unmanagedJars in Compile, state, ref, false, EvaluateTask.SystemProcessors)
			//			log.info("Unmanaged Jars: %s" format unmanagedJars)
			//
			//			val allDeps = EvaluateTask.evaluateTask(structure, Keys.allDependencies in Compile, state, ref, false, EvaluateTask.SystemProcessors)
			//			log.info("All deps: %s" format allDeps)
			//
			//			val projectDeps = EvaluateTask.evaluateTask(structure, Keys.projectDependencies in Compile, state, ref, false, EvaluateTask.SystemProcessors)
			//			log.info("Project dependencies: %s" format projectDeps)
		}
		state
	}

	/**
	 * lazy val eclipse = task {
	 * log.info("Creating eclipse project...")
	 * writeProjectFile(log) match {
	 * case None => writeClasspathFile(log)
	 * case ret@Some(_) => ret
	 * }
	 * }
	 *
	 * implicit lazy val projectNatureFormat = new Format[ProjectNature.Value] {
	 * def fromString(nature: String) = ProjectNature.valueOf(nature).getOrElse(ProjectNature.Scala)
	 * def toString(nature: ProjectNature.Value) = nature.toString
	 * }
	 *
	 * val eclipseName = settings
	 *
	 * def eclipseName = propertyOptional[String](projectName.value).value
	 * def projectDescription = propertyOptional[String](projectName.value + " " + projectVersion.value).value
	 * def includeProject = propertyOptional[Boolean](false).value
	 * def includePlugin = propertyOptional[Boolean](false).value
	 * def sbtDependency = propertyOptional[Boolean](false).value
	 * def pluginProject = propertyOptional[Boolean](false).value
	 * def eclipseProjectNature = propertyOptional[ProjectNature.Value](ProjectNature.Scala).value
	 *
	 * def findProjects(log: Logger): List[Project] = {
	 * Nil
	 * }
	 */
	/**
	 * Writes the .classpath file to filesystem.
	 * @return <code>Some(error)</code> when an error occures else returns <code>None</code>
	 */
	//	def writeClasspathFile(log: Logger): Option[String] = ClasspathFile(this, log).writeFile
	/**
	 * Writes the .project file to filesystem.
	 * @return <code>Some(error)</code> when an error occures else returns <code>None</code>
	 */
	//	def writeProjectFile(log: Logger): Option[String] = ProjectFile(this, log).writeFile
}
