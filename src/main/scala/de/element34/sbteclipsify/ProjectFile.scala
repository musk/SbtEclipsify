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

import java.io.File
import java.nio.charset.Charset._

import scala.xml._

/**
 * Defines the structure for a .project file.
 */
case class ProjectFile(ctx: ProjectCtx) {
	import CommandSupport.logger
	import Utils._
	
	val log = logger(ctx.state)
	val extracted = Project.extract(ctx.state)
	val structure = extracted.structure 
	val project = Project.getProject(ctx.ref, structure)
	
	def get[A] = setting[A](structure)_
	
	val name = get(ctx.ref, Keys.name, Compile).getOrElse("No name available")

	lazy val nature = Utils.nature(ctx.ref, structure, log)
	/**
	 * Writes the .project file to the file system
	 * @return <code>Some(error)</code> when an error occurred else returns <code>None</code>
	 */
	def writeFile: Option[String] = {
		lazy val projectContent = <projectDescription>
			<name>{ name }</name>
			<comment>{ get(ctx.ref, Eclipsify.description, Compile).getOrElse("") }</comment>
			<projects>{ createSubProjects }</projects>
			<buildSpec>
				{ nature.builder.distinct.map(b => <buildCommand><name>{ b }</name></buildCommand>) }
			</buildSpec>
			<natures>
				{ nature.nature.distinct.map(n => <nature>{ n }</nature>) }
			</natures>
		</projectDescription>

			def createSubProjects = ""

		get(ctx.ref, Keys.baseDirectory, Compile) match {
			case Some(s) =>
				val projectFile = (Path(s) / ".project").getAbsolutePath
				try {
					XML.save(projectFile, projectContent, "utf-8", true)
					None
				} catch {
					case e => Some("Error writing file %s:%n%s".format(projectFile, e))
				}
			case None => Some("Unable to determine base directory for project %s" format name)
		}
	}
}

