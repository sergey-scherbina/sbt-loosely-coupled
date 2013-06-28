import sbt._
import Keys._

object Unit1Build extends Build {

  lazy val unit1 = Project(id = "unit1",
    base = file(".")) settings (
      organization := "sergey.scherbina",
      name := "loosely-coupled.sample.unit1",
      scalaVersion := "2.10.1")

}