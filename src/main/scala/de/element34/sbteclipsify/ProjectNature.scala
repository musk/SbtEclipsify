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

sealed trait ProjectNature {
	case class ComposedProject(override val builder: List[String], override val nature: List[String], override val container: List[String]) extends ProjectNature
	def builder: List[String] = List.empty
	def nature: List[String] = List.empty
	def container: List[String] = List.empty
	def combine(other: ProjectNature): ProjectNature = ComposedProject(builder ++ other.builder,
		nature ++ other.nature, container ++ other.container)
}

case object JavaNature extends ProjectNature {
	override val builder = List("org.eclipse.jdt.core.javabuilder")
	override val nature = List("org.eclipse.jdt.core.javanature")
	override val container = List("org.eclipse.jdt.launching.JRE_CONTAINER")
}

case object ScalaNature extends ProjectNature {
	override val builder = List("org.scala-ide.sdt.core.scalabuilder")
	override val nature = List("org.scala-ide.sdt.core.scalanature")
	override val container = List("org.scala-ide.sdt.launching.SCALA_CONTAINER")
}

case object PluginNature extends ProjectNature {
	override val builder = List("org.eclipse.pde.ManifestBuilder", "org.eclipse.pde.SchemaBuilder")
	override val nature = List("org.eclipse.pde.PluginNature")
	override val container = List("org.eclipse.pde.core.requiredPlugins")
}

case object AndroidNature extends ProjectNature {
	override val builder = List("com.android.ide.eclipse.adt.ResourceManagerBuilder",
		"com.android.ide.eclipse.adt.PreCompilerBuilder",
		"org.eclipse.jdt.core.javabuilder",
		"com.android.ide.eclipse.adt.ApkBuilder")
	override val nature = List("org.eclipse.jdt.core.javanature",
			"com.android.ide.eclipse.adt.AndroidNature")
}

case object SbtEclipseIntegrationNature extends ProjectNature {
	override val nature = List("de.element34.sbt-eclipse-integration.nature")
}

object ProjectType {
	val Java: ProjectNature = JavaNature
	val Scala: ProjectNature = ScalaNature combine JavaNature
	val Android: ProjectNature = AndroidNature
	val ScalaAndroid: ProjectNature = ScalaNature combine AndroidNature
	val Plugin: ProjectNature = JavaNature combine PluginNature
	val ScalaPlugin: ProjectNature = ScalaNature combine JavaNature combine PluginNature
}
