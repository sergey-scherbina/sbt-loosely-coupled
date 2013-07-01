import sbt._

object BarBuild extends Build {

  val bar = Project(id = "bar",
    base = file("."))

}
