import sbt._
import Keys._

object MainBuild extends Build {

  val unit1 = file("unit1")

  val unit2 = file("unit2")

  val unit3 = file("unit3")

  lazy val main = Project(id = "main",
    base = file(".")) settings(
    organization := "sergey.scherbina",
    name := "loosely-coupled.sample.main",
    scalaVersion := "2.10.1") aggregate(unit1, unit2, unit3)

  override def buildLoaders = super.buildLoaders ++ Seq(
    LooselyCoupled.linkBuilds
  )

}