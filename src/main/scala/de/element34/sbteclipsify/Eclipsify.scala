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
import sbt.complete._
import sbt.complete.Parsers._

object Arguments extends Enumeration {
	type Arguments = Value
	val JAR_DEPS = Value("jar-deps")
	val WITH_SOURCES = Value("with-sources")
	val VERSION = Value("version")
}

/**
 * Defines the plugin with the "eclipse" task for sbt
 */
object Eclipsify extends Plugin {
	import CommandSupport.logger
	import Keys._
	import Arguments._
	
	override lazy val settings = Seq(commands += eclipse)

	val ECLIPSIFYVERSION = "0.10.0-SNAPSHOT"
	val description = SettingKey[String]("description")
	val nature = SettingKey[ProjectNature]("nature")

	lazy val JARS = Space ~> JAR_DEPS.toString
	lazy val SRCS = Space ~> WITH_SOURCES.toString
	lazy val VS = Space ~> VERSION.toString
	lazy val argParser = VS | JARS | SRCS | (JARS ~> SRCS ) | ( SRCS ~> JARS)
	lazy val argFormat: Parser[List[String]] = Parser.mapParser[String, List[String]](argParser, {f => List(f)})

	lazy val eclipse = Command("eclipse")(_ => argFormat) { (state, input) =>
		val log = logger(state)
		val args = input.map(Arguments.withName(_))
		log.debug("Args=%s".format(args))

		if (args.contains(VERSION)) {
			log.info("Version: %s".format(ECLIPSIFYVERSION))
		} else {
			val currProject = Project.current(state)
			log.debug("Current project: %s" format (currProject))

			val extracted = Project.extract(state)
			val structure = extracted.structure

				def get[A] = Utils.setting[A](structure)_

			get(currProject, Keys.baseDirectory, Compile).map(baseDir => {

				for (ref <- structure.allProjectRefs) {
					val ctx = ProjectCtx(baseDir, ref, state, args)
					val name = get(ref, Keys.name, Compile).getOrElse("<Unresolved>")
					ProjectFile(ctx).writeFile match {
						case None => log.info("written .project for %s" format name)
						case Some(err) => log.error("Unable to write .project for %s due to %s".format(name, err))
					}
					ClasspathFile(ctx).writeFile match {
						case None => log.info("written .classpath for %s" format name)
						case Some(err) => log.error("Unable to write .classpath for %s due to %s".format(name, err))
					}
				}
			}) match {
				case None => log.error("Base directory for %s cannot be resolved!".format(get(currProject, Keys.name, Compile).getOrElse("<Unresolved>")))
				case _ => log.info("You may now import your projects in Eclipse")
			}
		}
		state
	}
}
