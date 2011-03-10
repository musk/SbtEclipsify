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

import sbt._

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers

import de.element34.sbteclipsify._

class ClasspathFileTest extends FlatSpec with ShouldMatchers {

  "A ClasspathEntry" should "produce the xml for a classpathentry in the elcipse .classpath file" in {

    val entry = ClasspathEntry(Variable, "src/main/scala")
    val expected = """<classpathentry kind="var" path="src/main/scala" />"""
    entry.mkString should be(expected)

  }

  it should "produce srcpath entries" in {
    val entry = ClasspathEntry(Variable, "target/", "src/main/scala")
    val expected = """<classpathentry kind="var" path="target/" sourcepath="src/main/scala" />"""
    entry.mkString should be(expected)
  }

  it should "produce include and exlude filter entries" in {
    val incEntry = ClasspathEntry(Variable, "target/", FilterChain(IncludeFilter("**/*.scala")))
    val exEntry = ClasspathEntry(Variable, "target/", FilterChain(ExcludeFilter("**/*.scala")))
    val incExEntry = ClasspathEntry(Variable, "target/", FilterChain(IncludeFilter("**/*.scala"), ExcludeFilter("**/*.java")))

    val incExpected = """<classpathentry kind="var" path="target/" including="**/*.scala" />"""
    val exExpected = """<classpathentry kind="var" path="target/" excluding="**/*.scala" />"""
    val incExExpected = """<classpathentry kind="var" path="target/" including="**/*.scala" excluding="**/*.java" />"""

    incEntry.mkString should be(incExpected)
    exEntry.mkString should be(exExpected)
    incExEntry.mkString should be(incExExpected)
  }

  it should "produce container entries" in {
    val conEntry = ClasspathEntry(Container, "target/")
    val expected = """<classpathentry kind="con" path="target/" />"""
    conEntry.mkString should be(expected)
  }

  it should "produce output entries" in {
    val outEntry = ClasspathEntry(Output, "target/")
    val expected = """<classpathentry kind="output" path="target/" />"""
    outEntry.mkString should be(expected)
  }
}
