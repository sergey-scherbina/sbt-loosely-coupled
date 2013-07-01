import sbt._

object AppBuild extends Build {

  val app = Project(id = "app",
    base = file("."))

}
