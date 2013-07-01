import sbt._
import sbt.Keys._

object CoupledBuild extends Build {

  val buildRoot = file(System.getProperty("build.root", ".."))

  val foo = buildRoot / "foo"

  val bar = buildRoot / "bar"

  val app = buildRoot / "app"

  val coupled = "coupled-build"

  val sample = Project(id = coupled,
    base = file(".")) settings
    (name := coupled, publish := {},
      publishLocal := {}) settings
    (SamplePlugin.sampleSettings: _*) aggregate
    (foo, bar, app)


  val samplePlugin = buildRoot / "plugin"

  override def buildLoaders = super.buildLoaders ++ Seq(
    SbtLooselyCoupled.linkBuilds,
    SbtLooselyCoupled.addPlugins(samplePlugin)
  )

}