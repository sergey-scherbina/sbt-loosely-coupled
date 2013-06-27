import sbt._
import Keys._

object PluginBuild extends Build {

    lazy val plugin = Project(id = "plugin",
        base = file(".")) settings(sbtPlugin:=true)

}