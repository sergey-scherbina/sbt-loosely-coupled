package sample.loosely.project

import sbt._
import sbt.Keys._

object LooselyBuild extends Build {

  val buildRoot = file(System.getProperty("build.root", ".."))

  val foo = buildRoot / "foo"

  val bar = buildRoot / "bar"

  val app = buildRoot / "app"

  val loosely = "loosely-build"

  val sample = Project(id = loosely,
    base = file(".")) settings
    (name := loosely, publish := {},
      publishLocal := {}) settings
    (SamplePlugin.sampleSettings: _*) aggregate
    (foo, bar, app)

}