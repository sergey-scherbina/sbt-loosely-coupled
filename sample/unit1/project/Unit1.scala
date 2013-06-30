import sbt._

object Unit1 extends Build {

  val unit1 = Project(id = "unit1",
    base = file(".")) settings(
    SamplePlugin.sampleSettings: _*)

}
