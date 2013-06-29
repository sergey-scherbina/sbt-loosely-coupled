import sbt._
import sbt.Keys._
import sbt.Load._

object LooselyCoupled extends Plugin {

  def addPlugins(plugins: File*) = addPluginsFor(_ => true, plugins: _*)

  def addPluginsFor(add: BuildLoader.BuildInfo => Boolean,
                    plugins: File*) = BuildLoader.build {
    b => if (!add(b)) None
    else Some(() => loadUnit(b.uri, b.base, b.state,
      plugins.foldLeft(b.config) {
        (config, plugin) => loadGlobal(b.state, b.base,
          plugin.getAbsoluteFile, config)
      }))
  }

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

  def linkProject(builds: BuildDependencies, libraries: Seq[ModuleID],
                  project: ProjectRef, organization: String) = {
    val libs = libraries filter (_.organization == organization)
    val deps = builds.aggregate.keys.collect {
      case p if libs.exists(_.name == p.project) =>
        ResolvedClasspathDependency(p, None)
    }
    builds.addClasspath(project, deps.toSeq: _*)
  }

}