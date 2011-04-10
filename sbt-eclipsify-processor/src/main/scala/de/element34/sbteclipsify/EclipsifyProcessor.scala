package de.element34.sbteclipsify

import sbt._
import processor._

class EclipsifyProcessor extends BasicProcessor {
  def apply(project:Project, args:String) {
    new EclipsifyMixin {
      val eclipsifyProject = project
      import project.BooleanFormat
      lazy val eclipseName = project.propertyOptional[String](project.projectName.value)
      lazy val projectDescription = project.propertyOptional[String](project.projectName.value + " " + project.projectVersion.value)
      lazy val includeProject = project.propertyOptional[Boolean](false)
      lazy val includePlugin = project.propertyOptional[Boolean](false)
      lazy val sbtDependency = project.propertyOptional[Boolean](false)
      lazy val pluginProject = project.propertyOptional[Boolean](false)
      //lazy val customSrcPattern = project.propertyOptional[RegEx]()

      lazy val eclipseProjectNature = project.propertyOptional[ProjectNature.Value](ProjectNature.Scala)
    } writeEclipseFiles()
  }
}
