import sbt._
import sbt.Keys._
import sbt.Load._

object LooselyCoupled extends Plugin {

  type ProjectLinker = (BuildDependencies,
    Seq[ModuleID], ProjectRef, String) => BuildDependencies

  def linkBuilds = buildsLinker(linkProject)

  def buildsLinker(linker: ProjectLinker) =
    addSettings(toProjects = Seq(buildDependencies in Global <<=
      (buildDependencies in Global, libraryDependencies,
        thisProjectRef, organization)(linker)))

  def addSettings(toBuilds: Seq[Setting[_]] = Seq(),
                  toProjects: Seq[Setting[_]] = Seq(),
                  addLoaders: Seq[BuildLoader.Components] = Seq()) = {

    def buildˆ(b: Build) = new Build {
      override def buildLoaders = b.buildLoaders ++ addLoaders

      override def settings = b.settings ++ toBuilds

      override def projects = b.projects map (_.settings(toProjects: _*))
    }

    def defsˆ(d: LoadedDefinitions) = new LoadedDefinitions(
      d.base, d.target, d.loader, d.builds map buildˆ, d.buildNames)

    def unitˆ(u: BuildUnit) = new BuildUnit(
      u.uri, u.localBase, defsˆ(u.definitions), u.plugins)

    BuildLoader.transform(t => unitˆ(t.unit))
  }

  def linkProject(buildDependencies: BuildDependencies,
                  libraryDependencies: Seq[ModuleID],
                  thisProjectRef: ProjectRef,
                  organization: String) = {
    buildDependencies
  }


}