import sbt._

object Sample extends Build {

  val unit1 = file("unit1")

  val unit2 = file("unit2")

  val unit3 = file("unit3")

  val main = Project(id = "sample",
    base = file(".")) aggregate(unit1, unit2, unit3)

  override def buildLoaders = super.buildLoaders ++
    Seq(LooselyCoupled.linkBuilds)

}