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
    addSettings(inProjects = Seq(buildDependencies in Global <<=
      (buildDependencies in Global, libraryDependencies,
        thisProjectRef, organization)(linker)))

  def addSettings(inBuilds: Seq[Setting[_]] = Seq(),
                  inProjects: Seq[Setting[_]] = Seq(),
                  addLoaders: Seq[BuildLoader.Components] = Seq()) = {

    def buildFrom(b: Build) = new Build {
      override def buildLoaders = b.buildLoaders ++ addLoaders

      override def settings = b.settings ++ inBuilds

      override def projects = b.projects map (_.settings(inProjects: _*))
    }

    def defsFrom(d: LoadedDefinitions) = new LoadedDefinitions(
      d.base, d.target, d.loader, d.builds map buildFrom, d.buildNames)

    def unitFrom(u: BuildUnit) = new BuildUnit(
      u.uri, u.localBase, defsFrom(u.definitions), u.plugins)

    BuildLoader.transform(t => unitFrom(t.unit))
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