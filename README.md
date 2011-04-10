# SbtEclipsify

Plugin and Processor for sbt (http://code.google.com/p/simple-build-tool/) 0.7.x for creating .classpath and .project files for the Ecipse IDE (http://www.eclipse.org).
If you need to use this plugin with sbt version prior to 0.5.6 then you need to use version 0.4.1

## License
Just like sbt this software is distributed under the BSD License (http://www.opensource.org/licenses/bsd-license.php).

## Getting the Plugin
In order to use the plugin you can either download one of the provided jars in the dist folder or you can build it yourself.
Easiest is to add a dependency to the plugin in your plugin defintion `"de.element34" % "sbt-eclipsify-plugin" % "<version>"`.
Calling `reload` in sbt will pull in the needed dependency.
For setting up the plugin see "Using the Plugin in your own project" below.

### Downloading
You can find ready made jars in the dist folder on git. Download it and use sbt local dependency feature in order to install it in your project.

## Requirements
If you have scala sources in your project you need to have the [Scala Eclipse plugin](http://www.scala-tools.org/...) installed or else you will not be able to work with the generated project.
The plugin only adds the scala plugin nature to the project. The scala version used by eclipse cannot be set from this plugin but depends on the eclipse plugin's version. There is currently no way known to the author to use two different scala versions at the same time.

### Building
SbtEclipsify uses oh wonder sbt as the build tool.
After downloading it from git start sbt and do a update publish-local. This should compile all sources, run all tests and install the most recent version of the plugin to your local repo.
From now on you can simply use `"de.element34" % "sbt-eclipsify-plugin" % "<buildversion>"` to get the plugin for your own projects.

## Supported properties
The following properties can be added to the build.properties of your project in order to control the output of the plugin
`sbt.dependency`(default: false) => if set to true puts the sbt jar on the classpath of the project this is needed if you are creating actions or plugins for sbt

`eclipse.name` (default: name of the project) => The name of the Project to display in eclipse. Eclipse caches this property and will not update it upon refresh.
`project.description`(default: Projectname + Projectversion) => Set this to the text used to describe your project. this is directly transfered to the .projectfile's project description tag.

`include.project`(default: false) => if set to true the path to the project definition is added as a source folder to the classpath. This automatically puts the sbt jar on the classpath.

`include.plugin`(default: true) => if set to true the path to the plugin defintion is added as a source folder to the classpath. This automatically puts the sbt jar on the classpath.

`plugin.project` (default: false) => if set to true the project is setup to be a plugin project (osgi projects are handled as plugin projects in eclipse). This results in the plugin dependencies container being added to .classpath and the manifest and schema builder being added to .project.

`eclipse.project.nature` (default: scala) => the nature of the project can be either "java" or "scala". Unknown values are converted to "scala" 

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
      	   lazy val eclipse = "de.element34" % "sbt-eclipsify-plugin" % "0.7.1.szeiger-SNAPSHOT"
     }

This will enable your project to get the plugin in order to use it you need to add it to your project defintion.
Open your project definition file, something like "MySbtProject.scala" in "project/build/" folder, and add the Eclipsify trait.

    import sbt._
    import de.element34.sbteclipsify._

    class MySbtProject(info: ProjectInfo) extends DefaultProject(info) with Eclipsify {
      // the project definition here
    }

After reloading the project you should have a new action named "eclipse" which will generate a .project and a .classpath file in the MySbtProject folder.

Now all you need to do is import the Project into your Eclipse workspace as an existing Project and everything should work.

## Using SbtEclipsify as a Processor

In an existing sbt project, add the processor with this sbt command:

     *eclipse is de.element43 sbt-eclipsify-processor 0.7.1.szeiger-SNAPSHOT

You can now use the "eclipse" action in all your projects on the same machine.

## Known Issues
Crossbuilds are not supported officially but they might work (testing still pending).

## FUTURE
* Better documentation (as always :) )
* Add ability to fine tune generation
* Add ability to include directories besides those in test and main
* Add generation of subprojects like with eclipse:eclipse from maven
* Improve the test coverage (still need to figure out how to mock the sbt parts)
