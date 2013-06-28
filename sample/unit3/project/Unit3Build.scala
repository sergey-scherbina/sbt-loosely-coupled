import sbt._
import Keys._

object Unit3Build extends Build {

  lazy val unit3 = Project(id = "unit3",
    base = file(".")) settings (
      organization := "sergey.scherbina",
      name := "loosely-coupled.sample.unit3",
      scalaVersion := "2.10.1")
}