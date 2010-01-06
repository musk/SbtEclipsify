# SbtEclipsify

Plugin for sbt (http://code.google.com/p/simple-build-tool/) for creating .classpath and .project files for the Ecipse IDE (http://www.eclipse.org). 
It is currently in Beta state and is not feature complete.

## License
Just like sbt this software is distributed under the BSD License (http://www.opensource.org/licenses/bsd-license.php).

## Getting the Plugin
In order to use the plugin you either have to download the provided jar or you need to build it yourself (sorry no public release to a repo yet :( )

### Downloading
You can find ready made jars in the dist folder on git. Download it and use sbt local dependency feature in order to install it in your project.

## Requirements
If you have scala sources in your project you need to have the Scala Eclipse plugin installed or else you will not be able to work with the generated project.
This plugin is currently only compiled against Scala 2.7.7 and there is no crossbuild in place as of yet.

### Building
SbtEclipsify uses oh wonder sbt as the build tool. 
After downloading it from git start sbt and do a publish-local. This should compile all sources, run all tests and install the most recent version of the plugin to your local repo. 
From now on you can simply use `"de.element34" % "sbteclipsify" % "<buildversion>"` to get the plugin for your own projects.

## Supported properties
The following properties can be added to the build.properties of your project in order to control the output of the plugin
`sbt.dependency`(default: false) => if set to true puts the sbt jar on the classpath of the project this is needed if you are creating actions or plugins for sbt
`project.description`(default: Projectname + Projectversion) => Set this to the text used to describe your project. this is directly transfered to the .projectfile's project description tag.
`include.project`(default: false) => if set to true the path to the project definition is added as a source folder to the classpath. 
`include.plugin`(default: true) => if set to true the path to the plugin defintion is added as a source folder to the classpath.

## Using the Plugin in your own project
You need to create a plugin definition in your sbt project. 
The plugin definition can be any Scala class that extends PluginDefinition and is located in the plugins directory in the projects subfolder of your sbt project.

Example:
If your project is located at MySbtProject/

then create the plugins directory
`mkdir MySbtProject/project/plugins`

next create a file name MySbtProjectPlugins.scala and add the following text to it:

     import sbt._

     class MySbtProjectPlugins(info: ProjectInfo) extends PluginDefinition(info) {
      	   lazy val eclipse = "de.element34" % "sbteclipsify" % "0.2.0"
     }

This will enable your project to get the plugin in order to use it you need to add it to your project defintion. 
Open your project definition file, most likley "MySbtProject.scala" in "project/build/" folder, and add the SbtEclipsifyPlugin trait.

    import sbt._
    import de.element34.sbteclipsify._

    class MySbtProject(info: ProjectInfo) extends DefaultProject(info) with SbtEclipsifyPlugin {
      // the project definition here
    }

After reloading the project you should have a new action named "eclipse" which will generate a .project and a .classpath file in the MySbtProject folder.

Now all you need to do is import the Project into your Eclipse workspace as an existing Project and everything should work.

## Known Issues
None as of yet but then again this is still very beta and not too well tested and there are still a lot of missing features

## FUTURE
* Make the project buildable from a simple download
* Better documentation (as always :) )
* Add ability to fine tune generation
* Add ability to include directories besides those in test and main
* Add generation of subprojects like with eclipse:eclipse from maven
* Improve the test coverage (still need to figure out how to mock the sbt parts) 
* Add sources when available to classepathentries


