import sbt._
import sbt.Keys._

object Sample extends Build {

  val unit1 = file("unit1")

  val unit2 = file("unit2")

  val unit3 = file("unit3")

  val samplePlugin = file("plugin")

  val sample = Project(id = "sample",
    base = file(".")) settings
    (publish := {}, publishLocal := {}) settings
    (SamplePlugin.sampleSettings: _*) aggregate
    (unit1, unit2, unit3)

  override def buildLoaders = super.buildLoaders ++ Seq(
    SbtLooselyCoupled.linkBuilds,
    SbtLooselyCoupled.addPlugins(samplePlugin)
  )

}