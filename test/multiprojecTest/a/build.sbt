libraryDependencies += "org.scalaz" %% "scalaz-core" % "6.0" 

//unmanagedJars in Compile += Attributed.blank(file("./libs/jodatime-on-scala_2.8.0-0.2.0.jar"))
unmanagedBase <<= baseDirectory(base => base/"libs")
