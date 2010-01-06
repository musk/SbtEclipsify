package de.element34.sbteclipsify

import org.scalatest._
import org.scalatest.matchers._
import org.scalatest.matchers.ShouldMatchers._

import de.element34.sbteclipsify._

class ClasspathFileTest extends FlatSpec with ShouldMatchers {

//   "A ClasspathFile" should "produce a valid xml file with classpathentries" {
//     val expected = """<?xml version="1.0" encoding="UTF-8" ?>""" +
//     <classpath>
//       <classpathentry kind="src" path="src1/" including="**/*.scala" />
//       <classpathentry kind="lib" path="lib/" />
//       <classpathentry kind="cont" path="container" />
//     </classpath>.mkString

//     /*
//      * TODO mock projectinfo with the above values
//      *      ProjectInfo is in the filed info in Project which is passed to ClasspathFile
//      *      Project needs a mixin of type MavenStylePaths
//      *      val project: Project = new Project(???) with MavenStylePaths
//      *      ClasspathFile(project, new Logger())
//      */
//   }

  "A ClasspathEntry" should "produce the xml for a classpathentry in the elcipse .classpath file" in {

    val entry = ClasspathEntry(Variable, "src/main/scala")
    val expected = """<classpathentry kind="var" path="src/main/scala" />"""
    entry.mkString should be (expected)

  }

  it should "produce srcpath entries" in {
    val entry = ClasspathEntry(Variable, "target/", "src/main/scala")
    val expected = """<classpathentry kind="var" path="target/" sourcepath="src/main/scala" />"""
    entry.mkString should be (expected)
  }

  it should "produce include and exlude filter entries" in {
    val incEntry = ClasspathEntry(Variable, "target/", FilterChain(IncludeFilter("**/*.scala")))
    val exEntry = ClasspathEntry(Variable, "target/", FilterChain(ExcludeFilter("**/*.scala")))
    val incExEntry = ClasspathEntry(Variable, "target/", FilterChain(IncludeFilter("**/*.scala"), ExcludeFilter("**/*.java")))

    val incExpected = """<classpathentry kind="var" path="target/" including="**/*.scala" />"""
    val exExpected = """<classpathentry kind="var" path="target/" excluding="**/*.scala" />"""
    val incExExpected = """<classpathentry kind="var" path="target/" including="**/*.scala" excluding="**/*.java" />"""

    incEntry.mkString should be (incExpected)
    exEntry.mkString should be (exExpected)
    incExEntry.mkString should be (incExExpected)
  }

  it should "produce container entries" in {
    val conEntry = ClasspathEntry(Container, "target/")
    val expected = """<classpathentry kind="con" path="target/" />"""
    conEntry.mkString should be (expected)
  }

  it should "produce output entries" in {
    val outEntry = ClasspathEntry(Output, "target/")
    val expected = """<classpathentry kind="output" path="target/" />"""
    outEntry.mkString should be (expected)
  }
}
