import sbt._
import sbt.Keys._
import sbt.Load._

object LooselyCoupled extends Plugin {

  def linkBuilds = addSettings(toProjects = Seq(
    buildDependencies in Global <<= (buildDependencies in Global,
      libraryDependencies, thisProjectRef)(linkBuildDependencies)
  ))

  def addSettings(toBuilds: Seq[Setting[_]] = Seq(),
                  toProjects: Seq[Setting[_]] = Seq(),
                  addLoaders: Seq[BuildLoader.Components] = Seq()) =
    BuildLoader.transform {
      info =>
        def unitˆ(u: BuildUnit) = new BuildUnit(
          u.uri, u.localBase, defsˆ(u.definitions), u.plugins)

        def defsˆ(d: LoadedDefinitions) = new LoadedDefinitions(
          d.base, d.target, d.loader, d.builds map buildˆ, d.buildNames)

        def buildˆ(b: Build) = new Build {
          override def buildLoaders = b.buildLoaders ++ addLoaders

          override def settings = b.settings ++ toBuilds

          override def projects = b.projects map (_.settings(toProjects: _*))
        }

        unitˆ(info.unit)
    }

  def linkBuildDependencies(buildDependencies: BuildDependencies,
                            libraryDependencies: Seq[ModuleID],
                            thisProjectRef: ProjectRef) = {

    println("link build dependencies")

    buildDependencies
  }


}