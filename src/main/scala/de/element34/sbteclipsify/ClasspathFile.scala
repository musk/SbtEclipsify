package de.element34.sbteclipsify

import scala.xml._
import sbt.Path

abstract class Filter(pattern: String)
case class IncludeFilter(pattern: String) extends Filter(pattern)
case class ExcludeFilter(pattern: String) extends Filter(pattern)

abstract class Kind {
  def toString: String
}
case object Variable extends Kind {
  override def toString = "var"
}
case object Container extends Kind {
  override def toString = "con"
}
case object Output extends Kind {
  override def toString = "output"
}

case class ClasspathEntry(kind: Kind, path: String, srcpath: Option[String], filter: List[Filter]) {

  def this(kind: Kind, path: Path, srcpath: Option[String], filter: List[Filter]) = this(kind, path.toString, srcpath, filter)

  def toXml = {
    var entry = <classpathentry kind="{kind.toString}" path="{path}" />
    entry = entry % writeSrcPath(srcpath)
    entry = entry % writeFilters(filter)
  }

  def writeFilters(filter: List[Filter]): MetaData = {
    def writeFilter(filter: List[Filter], result: MetaData): MetaData = {
      filter match {
	case IncludeFilter(incPattern) :: rest =>
	  writeFilter(rest, new UnprefixedAttribute("including",  incPattern,  result))
	case ExcludeFilter(exPattern) :: rest =>
	  writeFilter(rest, new UnprefixedAttribute("excluding" , exPattern, result)) 
	case Nil => 
	  result
      }
    }
    writeFilter(filter, Null)
  }

  def writeSrcPath(srcpath: Option[String]): MetaData = {
    srcpath match { 
      case Some(text) => new UnprefixedAttribute("sourcepath", text, Null)
      case None => Null
    }
  }
}

