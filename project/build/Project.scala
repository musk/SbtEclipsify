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
import sbt._
import de.element34.sbteclipsify._

class SbtEclipsifyPluginProject(info: ProjectInfo) extends PluginProject(info) with Eclipsify {
	override def compileOptions =  super.compileOptions ++ (Unchecked :: Deprecation :: Nil)
  	override def mainResources = super.mainResources +++ "NOTICE" +++ "LICENSE" +++ (path("licenses") * "*")

  	val scalaSnapshotToolsRepository = "Scala Tools Repository" at "http://nexus.scala-tools.org/content/repositories/snapshots/"

  	lazy val scalaTest = "org.scalatest" % "scalatest" % "1.2" % "test"

  	override def managedStyle = ManagedStyle.Maven

  	val credPath = Path.userHome / ".credentials"
  	Credentials(credPath, log)

  	val publishTo = "Sonatype Nexus Repository Manager" at "http://nexus.scala-tools.org/content/repositories/releases/"

  	override def pomExtra =
  		<description>sbt-eclipsify is a plugin provided under a BSD-License. It generates .classpath and .project files for the Eclipse IDE from a sbt project.</description>
  		<developers>
  			<developer>
  				<id>slanger</id>
  				<name>Stefan Langer</name>
  				<email>mailtolanger@googlemail.com</email>
  				<roles>
  					<role>Project Lead</role>
  					<role>Developer</role>
  				</roles>
  				<timezone>2</timezone>
  			</developer>
  		</developers>
  		<licenses>
	  		<license>
		  		<name>BSD-License</name>
		  		<url></url>
		  		<distribution>repo</distribution>
		  	</license>
		</licenses>
		<scm>
			<connection>git@github.com:musk/SbtEclipsify.git</connection>
			<tag>{projectVersion.value}</tag>
			<url>http://www.github.com/musk/SbtEclipsify</url>
		</scm>
		<organisation>
			<name>Element34</name>
			<url>http://www.element34.de</url>
		</organisation>
}
