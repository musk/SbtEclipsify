# SbtEclipsify

Plugin for [sbt 0.10.1[[5] for creating __.classpath__ and __.project__ files for the [Ecipse IDE][2].
If you need to use this plugin with sbt version prior to 0.10.0 then you need to use version 0.8.x

## License
Just like sbt this software is distributed under the [BSD License][3].

## Features
 
 * Capable of generating projects for 
     
     * __Android__
     * __Scala__
     * __Java__
     * __SbtEclipseIntegration__
     * more to come ...
 
     
 * Only depends on sbt and scala-library producing no dependency hell when mixing with other plugins.
 * Open for anybody to contribute on [sbteclipsify github][1]
 * multiproject support

## Installing the plugin
To use the plugin you can either define it globally making it available to all sbt builds or you can install it local to your project.
Simply add the following dependency to your libraryDependecies

	libraryDependencies <+= (libraryDependencies, sbtVersion) { (deps, version) => 
		"de.element34" %% "sbt-eclipsify" % "0.10.0-SNAPSHOT"
	}

For setting up the plugin see "Using the Plugin in your own project" below.

## Requirements 
If you have scala sources in your project you need to have the [Scala Eclipse plugin][4] installed or else you will not be able to work with the generated project.
The plugin only adds the scala plugin nature to the project. The scala version used by eclipse cannot be set from this plugin but depends on the eclipse plugin's version. There is currently no way known to the author to use two different scala versions at the same time.

### Building
SbtEclipsify uses oh wonder [sbt][5] as the build tool.
After cloning it from git, start sbt and do a  _update_ _publish-local_. This should compile all sources, run all tests and install the most recent version of the plugin to your local repo.

See "Installing the Plugin" for instruction of installing it in your projects.

## Setup of the plugin
The plugin can be configured for the type of project you want to generate. This can be achived by specifing the nature your project has. 
For this purpose the plugin introduces a new setting
 
    SettingsKey[de.element34.sbteclipsify.ProjectNature]("nature")

All available natures can be viewed in the corresponding source file for convenience the following natures are predefined in _de.element34.sbteclipsify.ProjectType_
 
 * __Java__ A simple java project
 * __Scala__ A Scala project this is the default
 * __Android__ A Android plugin using Java
 * __ScalaAndroid__ A Android plugin using Scala
 * __Plugin__ A Eclipse plugin project using Java
 * __ScalaPlugin__ A Eclipse plugin project using Scala

To add simply add the following line to your build.sbt file

	nature := de.element34.sbteclipsify.ProjectType.Java

You can also combine different natures to produce more complex natures
 
	import de.element34.sbteclipsify.ProjectNature._
	nature := JavaNature combine ScalaNature combine AndroidNature

## Using the Plugin 
When everything is setup correctly you will have a new action named __eclipse__. 

The following options are available for the eclipse command:
 
 * __jar-deps__ - Create dependenten projects as libraries that refer to the jars in the build directory of those projects. Projects are build when nessecary.
 * __with-sources__ - Create source entries for libariers which provide sources. This option downloads the sources from the repository when nessecary. 

If everything went well you can import the Project via "File -> Import... -> General/Existing Projects into Workspace" into your Eclipse workspace.

If you encounter an error please report an issue at [sbteclipsify github][1]

## Reporting issues
Issues can be reported at [sbteclipsify github][1]

## Known Issues
None known yet ...

[1]: http://github.com/musk/SbtEclipsify
[2]: http://www.eclipse.org
[3]: http://www.opensource.org/licenses/bsd-license.php
[4]: http://www.scalaide.org/
[5]: https://github.com/harrah/xsbt/wiki
