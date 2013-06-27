import sbt._
import sbt.Keys._

object SamplePlugin extends Plugin {

    def commonSettings = Seq(
	scalaVersion := "2.10.1"
    )

}