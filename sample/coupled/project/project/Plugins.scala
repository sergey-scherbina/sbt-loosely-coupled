import sbt._

object Plugins extends Build {

  val samplePlugin = file("../../plugin")

  lazy val plugins = Project(id = "plugins",
    base = file(".")) dependsOn 
    (samplePlugin) settings
    (SbtLooselyCoupled.internalResolver) 

}