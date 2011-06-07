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

import scala.xml._

import sbt._

object Utils {
  /**
   * cast <code>Project</code> to <code>Eclipsify</code>
   */
  implicit def toEclipsify(project: Project): Eclipsify = project.asInstanceOf[Eclipsify]

  /**
   * Writes <code>NodeSeq</code> getting access to <code>Eclipsify</code> parameters by converting implicitly passed parameter project to <code>Eclipsify</code>
   */
  def writeNodeSeq(body: Eclipsify => NodeSeq)(implicit project: Project): NodeSeq = {
    writeNodeSeq(true)(body)(project)
  }

  /**
   * Writes a <code>NodeSeq</code> if the predicate is true else returns an empty <code>NodeSeq</code>. Automatically converts implicitly passed project to Eclipsify trait to access parameters.
   */
  def writeNodeSeq(predicate: => Boolean)(body: Eclipsify => NodeSeq)(implicit project: Project): NodeSeq = {
    if (predicate) {
      body(toEclipsify(project))
    } else NodeSeq.Empty
  }
}