import sbt._
import sbt.Keys._

object SamplePlugin extends Plugin {

  def commonSettings = Seq(
    organization := "sergey.scherbina",
    scalaVersion := "2.10.1")

}