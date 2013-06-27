import sbt._
import Keys._

object Unit1Build extends Build {

    lazy val unit1 = Project(id = "unit1",
        base = file(".")) settings(
        SamplePlugin.commonSettings:_*)

}