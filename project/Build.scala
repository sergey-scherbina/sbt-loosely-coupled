import sbt._
import sbt.Keys._

object Build extends sbt.Build {

  object V {
    val name = "sbt-loosely-coupled"
    val org = "sergey-scherbina"
    val ver = "0.1"
  }

  val project = Project(id = V.name,
    base = file(".")) settings(
    name := V.name,
    organization := V.org,
    version := V.ver,
    sbtPlugin := true)
}
