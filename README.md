# SbtEclipsify

Plugin for sbt (http://code.google.com/p/simple-build-tool/) 0.10.1 for creating .classpath and .project files for the Ecipse IDE (http://www.eclipse.org).
If you need to use this plugin with sbt version prior to 0.10.0 then you need to use version 0.8.x

## License
Just like sbt this software is distributed under the BSD License (http://www.opensource.org/licenses/bsd-license.php).

## Features
 
 * Capable of generating projects for 
  * Android
  * Scala
  * Java
  * SbtEclipseIntegration
  * more to come soon...
 * Only depends on sbt and scala-library no dependencies hell when mixing with other plugins.
 * Open for anybody to contribute on [sbteclipsify github][1]
 * true multiproject support

## Installing the plugin
To use the plugin you can either define it globally making it available to all sbt builds or you an install it local to your project.
Simply add the following dependency to your libraryDependecies

 libraryDependencies <<= (libraryDependencies, sbtVersion) { (deps, version) => 
        deps :+ ("de.element34" %% "sbt-eclipsify" % "0.10.0-SNAPSHOT")
 }

For setting up the plugin see "Using the Plugin in your own project" below.

### Downloading
You can find ready made jars in the dist folder on git. Download it and use sbt local dependency feature in order to install it in your project.

## Requirements 
If you have scala sources in your project you need to have the [Scala Eclipse plugin](http://www.scala-tools.org/...) installed or else you will not be able to work with the generated project.
The plugin only adds the scala plugin nature to the project. The scala version used by eclipse cannot be set from this plugin but depends on the eclipse plugin's version. There is currently no way known to the author to use two different scala versions at the same time.

### Building
SbtEclipsify uses oh wonder sbt (0.10.1) as the build tool.
After cloning it from git, start sbt and do a `update `publish-local. This should compile all sources, run all tests and install the most recent version of the plugin to your local repo.

See `Installing the Plugin for instruction of installing it in your projects.

## Setup of the plugin
The plugin can be configured for the type of project you want to generate. This can be achived by specifing the nature your project has. For this purpose the plugin introduces a new setting
SettingsKey[de.element34.sbteclipsify.ProjectNature]("nature")

All available natures can be viewed in the corresponding source file for convinience the following natures are predefined in de.element34.sbteclipsify.ProjectType
 
 * Java => A simple java project
 * Scala => A Scala project this is the default
 * Android => A Android plugin using Java
 * ScalaAndroid => A Android plugin using Scala
 * Plugin => a Eclipse plugin project using Java
 * ScalaPlugin => a Eclipse plugin project using Scala

To add simply add the following line to your build.sbt file
 nature := de.element34.sbteclipsify.ProjectType.Java
You can also combine different natures to produce more complex natures
 import de.element34.sbteclipsify.ProjectNature._
 nature := JavaNature combine ScalaNature combine AndroidNature

## Using the Plugin 
When everything is setup correctly you will have a new action named "eclipse". 
Calling it will generate a .project and a .classpathfile in the rootdirectory of your projects. All dependent projects will also be generated. 

Now all you need to do is import the Project into your Eclipse workspace as an existing Project and everything should work.

## Reporting issues
Issues can be reported at [sbteclipsify github][1]

## Known Issues
None known yet ...

[1]: http://github.com/musk/SbtEclipsify
