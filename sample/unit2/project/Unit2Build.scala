import sbt._
import Keys._

object Unit2Build extends Build {

    lazy val unit2 = Project(id = "unit2",
        base = file(".")) settings(
        SamplePlugin.commonSettings:_*)

}