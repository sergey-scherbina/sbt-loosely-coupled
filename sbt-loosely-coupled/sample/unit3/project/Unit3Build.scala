import sbt._
import Keys._

object Unit3Build extends Build {

    lazy val unit3 = Project(id = "unit3",
        base = file(".")) settings(
        SamplePlugin.commonSettings:_*)

}