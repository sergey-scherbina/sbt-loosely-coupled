import sbt._

object AddPlugin extends Build {

  val looselyCoupled = file("../..")

  lazy val addPlugin = Project(id = "add-plugin",
    base = file(".")) dependsOn (looselyCoupled)

}