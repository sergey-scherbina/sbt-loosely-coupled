import sbt._
import Keys._

object Unit2Build extends Build {

    lazy val unit2 = Project(id = "unit2",
        base = file(".")) settings (
      organization := "sergey.scherbina",
      name := "loosely-coupled.sample.unit2",
      scalaVersion := "2.10.1")
}