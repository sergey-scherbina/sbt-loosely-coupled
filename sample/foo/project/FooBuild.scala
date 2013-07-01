import sbt._

object FooBuild extends Build {

  val foo = Project(id = "foo",
    base = file("."))

}
