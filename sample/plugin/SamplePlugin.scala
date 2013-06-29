import sbt._
import sbt.Keys._

object SamplePlugin extends Plugin {

  def sampleSettings = Seq(
    organization := "sample",
    version := "0.1",
    scalaVersion := "2.10.1"
  )

}