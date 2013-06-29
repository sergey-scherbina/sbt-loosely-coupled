import sbt._

object LinkPlugin extends Build {

  val looselyCoupled = file("../..")

  lazy val plugins = Project(id = "linkPlugin",
    base = file(".")) dependsOn (looselyCoupled)

}