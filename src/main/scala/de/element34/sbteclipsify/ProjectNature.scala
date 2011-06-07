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
	case class ComposedProject(override val builder: Set[String], override val nature: Set[String]) extends ProjectNature
	def builder: Set[String] = Set.empty
	def nature: Set[String] = Set.empty
	def combine(other: ProjectNature): ProjectNature = ComposedProject(builder ++ other.builder, nature ++ other.nature)
}

case object JavaNature extends ProjectNature {
	override val builder = Set("org.eclipse.jdt.core.javabuilder")
	override val nature = Set("org.eclipse.jdt.core.javanature")
}

case object ScalaNature extends ProjectNature {
	override val builder = Set("org.scala-ide.sdt.core.scalabuilder")
	override val nature = Set("org.scala-ide.sdt.core.scalanature")
}

case object PluginNature extends ProjectNature {
	override val builder = Set("org.eclipse.pde.ManifestBuilder", "org.eclipse.pde.SchemaBuilder")
	override val nature = Set("org.eclipse.pde.PluginNature")
}

case object AndroidNature extends ProjectNature {
	override val builder = Set("com.android.ide.eclipse.adt.ResourceManagerBuilder",
		"com.android.ide.eclipse.adt.PreCompilerBuilder",
		"com.android.ide.eclipse.adt.ApkBuilder")
	override val nature = Set("com.android.ide.eclipse.adt.AndroidNature")
}

object ProjectType {
	val Java: ProjectNature = JavaNature
	val Scala: ProjectNature = ScalaNature combine JavaNature
	val Android: ProjectNature = JavaNature combine AndroidNature
	val ScalaAndroid: ProjectNature = ScalaNature combine JavaNature combine AndroidNature
	val Plugin: ProjectNature = JavaNature combine PluginNature
	val ScalaPlugin: ProjectNature = ScalaNature combine JavaNature combine PluginNature
}
