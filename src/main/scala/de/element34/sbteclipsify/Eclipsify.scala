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
	
	lazy val eclipse: Command = Command.command("eclipse") { state =>

		val extracted = Project.extract(state)
		val structure = extracted.structure

		for (ref <- structure.allProjectRefs) {
			val name = (Keys.name in (ref, Compile) get structure.data).getOrElse("<Information unavailable>")
			logger(state).info("Creating eclipse project %s ..." format name)

			logger(state).info("created!")
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
