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

import sbt._

import java.io.File
import java.nio.charset.Charset._

import scala.xml._

/**
 * Defines the structure for a .project file.
 */
class ProjectFile(project: Project, log: Logger) {
  /**
   * Writes the .project file to the file system
   * @return <code>Some(error)</code> when an error occurred else returns <code>None</code>
   */
  def writeFile: Option[String] = {
	import Utils._

    val scalaBuilder = "org.scala-ide.sdt.core.scalabuilder"
    val javaBuilder = "org.eclipse.jdt.core.javabuilder"
  
    val androidBuilder = List("org.eclipse.jdt.core.javabuilder",
          "com.android.ide.eclipse.adt.ResourceManagerBuilder",   
          "com.android.ide.eclipse.adt.PreCompilerBuilder",   
          "com.android.ide.eclipse.adt.ApkBuilder")
  
    val manifestBuilder = "org.eclipse.pde.ManifestBuilder"
    val schemaBuilder = "org.eclipse.pde.SchemaBuilder"
    val scalaNature = "org.scala-ide.sdt.core.scalanature"
    val javaNature = "org.eclipse.jdt.core.javanature"
    val pluginNature = "org.eclipse.pde.PluginNature"
    val androidNature = "com.android.ide.eclipse.adt.AndroidNature"

    lazy val projectFile: File = project.info.projectPath / ".project" asFile
    lazy val projectContent = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n" +
<projectDescription>
  <name>{getProjectName}</name>
  <comment>{getProjectDescription}</comment>
  <projects>{createSubProjects}</projects>
  <buildSpec>
  
  {getBuilderName}
    {getPluginXml}
  </buildSpec>
  <natures>
    {getMainNatures}
    {getPluginNature}
  </natures>
</projectDescription>

  def getBuilderName = get(_.eclipseProjectNature) match {
    case ProjectNature.Scala => writeNodeSeq { _ => 
      <buildCommand><name>scalaBuilder</name></buildCommand>
    }
    case ProjectNature.Java => writeNodeSeq { _ => 
      <buildCommand><name>javaBuilder</name></buildCommand>
    } 
    case ProjectNature.Android => 
    androidBuilder.map { s => <buildCommand><name>{s}</name></buildCommand> }    
      
      
  }

  def getMainNatures = get(_.eclipseProjectNature) match {
    case ProjectNature.Scala => writeNodeSeq { _ =>
      <nature>{scalaNature}</nature>
      <nature>{javaNature}</nature>
    }
    case ProjectNature.Java => writeNodeSeq { _ =>
      <nature>{javaNature}</nature>
    }
    case ProjectNature.Android => writeNodeSeq { _ =>
      <nature>{javaNature}</nature>
      <nature>{androidNature}</nature>
    }
  }
  
	def getPluginNature: NodeSeq = writeNodeSeq(get(_.pluginProject)){ _ =>
		<nature>{pluginNature}</nature>
	}

	def getPluginXml: NodeSeq = writeNodeSeq(get(_.pluginProject)){ _ =>
		<buildCommand>
		  <name>{manifestBuilder}</name>
		</buildCommand>
		<buildCommand>
		  <name>{schemaBuilder}</name>
		</buildCommand>
	}

    def getProjectName: String = get(_.eclipseName)
    def getProjectDescription: String =  get(_.projectDescription)

    implicit def sbtProject: Project = project
    /**
     * Creates dependent sub projects
     */
    def createSubProjects = ""

    FileUtilities.touch(projectFile, log) match {
    	case Some(error) =>
    		Some("Unable to write project file " + projectFile+ ": " + error)
    	case None =>
    		FileUtilities.write(projectFile, projectContent, forName("UTF-8"), log)
    	}
   	}
}

/**
 * Factory for creating <code>ProjectFile</code> instances
 */
object ProjectFile {
  def apply(project: Project, log: Logger) = new ProjectFile(project, log)
}
