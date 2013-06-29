import sbt._

object LinkPlugins extends Build {

  val looselyCoupled = file("../..")

  val samplePlugin = file("../plugin")

  lazy val plugins = Project(id = "linkPlugins",
    base = file(".")) dependsOn(
    looselyCoupled, samplePlugin)

}