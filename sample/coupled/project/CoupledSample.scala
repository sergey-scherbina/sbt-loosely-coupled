import sbt._
import sbt.Keys._

object CoupledSample extends Build {

  val buildRoot = file(System.getProperty("build.root", ".."))

  val unit1 = buildRoot / "unit1"

  val unit2 = buildRoot / "unit2"

  val unit3 = buildRoot / "unit3"

  val sample = Project(id = "coupled-sample",
    base = file(".")) settings
    (name := "coupled-sample",
      publish := {}, publishLocal := {}) settings
    (SamplePlugin.sampleSettings: _*) aggregate
    (unit1, unit2, unit3)


  val samplePlugin = buildRoot / "plugin"

  override def buildLoaders = super.buildLoaders ++ Seq(
    SbtLooselyCoupled.linkBuilds,
    SbtLooselyCoupled.addPlugins(samplePlugin)
  )

}