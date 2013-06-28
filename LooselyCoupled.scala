import sbt._
import sbt.Keys._
import sbt.Load._

object LooselyCoupled extends Plugin {

  def linkBuilds = BuildLoader.transform(addSettings(
    buildDependencies in Global <<= (buildDependencies in Global,
      libraryDependencies, thisProjectRef)(linkBuildDependencies)
  ))

  def addSettings(add: Setting[_]*)(info: BuildLoader.TransformInfo) = {

    def toBuild(b: Build) = new Build {
      override def buildLoaders = b.buildLoaders

      override def settings = b.settings

      override def projects = b.projects map (_.settings(add: _*))
    }

    def toDefs(d: LoadedDefinitions) = new LoadedDefinitions(
      d.base, d.target, d.loader, d.builds map toBuild, d.buildNames
    )

    def toUnit(u: BuildUnit) = new BuildUnit(
      u.uri, u.localBase, toDefs(u.definitions), u.plugins
    )

    toUnit(info.unit)
  }

  def linkBuildDependencies(buildDependencies: BuildDependencies,
                            libraryDependencies: Seq[ModuleID],
                            thisProjectRef: ProjectRef) = {

    println("link build dependencies")

    buildDependencies
  }


}