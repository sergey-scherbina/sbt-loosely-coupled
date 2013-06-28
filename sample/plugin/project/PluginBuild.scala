import sbt._
import Keys._

object PluginBuild extends Build {

    lazy val plugin = Project(id = "plugin",
        base = file(".")) settings(
        	organization := "sergey.scherbina",
			name := "loosely-coupled.sample.plugin",
        	sbtPlugin:=true
        )

}