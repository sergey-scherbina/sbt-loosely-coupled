import sbt._
import sbt.Keys._
import sbt.Load._
import sbt.ResolvedClasspathDependency
import scala.Some
import scala.Some
import scala.Some

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

  def buildsLinker(linker: ProjectLinker) = addSettings(
    inProjects = Seq(buildDependencies in Global <<=
      (buildDependencies in Global, libraryDependencies,
        thisProjectRef, organization)(linker)),
    inBuilds = Seq(onLoad in Global := onLoadBuild)
  )

  def onLoadBuild(state: State) = state get stateBuildStructure flatMap {
    structure => (buildDependencies in Global) get structure.data map {
      dependencies => state.copy(attributes = state.attributes.put(
        stateBuildStructure, linkBuildStructure(structure, dependencies)))
    }
  } getOrElse state


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

  def linkBuildStructure(structure: BuildStructure,
                         dependencies: BuildDependencies) = {

    def unitFrom(u: BuildUnit) = {

      def depends(p: Project) = {
        dependencies.classpath.get(ProjectRef(u.uri, p.id)).map {
          _.foldLeft(p)((p, d) => p.dependsOn(d.project))
        }.getOrElse(p)
      }

      def buildFrom(b: Build) = new Build {

        override def buildLoaders = b.buildLoaders

        override def settings = b.settings

        override def projects = b.projects map depends

      }

      def defsFrom(d: LoadedDefinitions) = new LoadedDefinitions(
        d.base, d.target, d.loader, d.builds map buildFrom, d.buildNames)

      new BuildUnit(u.uri, u.localBase, defsFrom(u.definitions), u.plugins)
    }

    def definedFrom(buildUri: URI)(d: (String, ResolvedProject)) = {
      val (u, p) = d
      def rdeps(ds: Seq[ClasspathDep[ProjectRef]]) = ds.map {
        cp => ResolvedClasspathDependency(cp.project, cp.configuration)
      }
      def depends = dependencies.classpath.getOrElse(
        ProjectRef(buildUri, p.id), Seq())
      (u, Project.resolved(p.id, p.base, p.aggregate,
        rdeps(p.dependencies) ++ rdeps(depends),
        p.delegates, p.settings, p.configurations))
    }

    def mapUnit(u: (URI, LoadedBuildUnit)) = (u._1, new LoadedBuildUnit(
      unitFrom(u._2.unit), u._2.defined map definedFrom(u._1),
      u._2.rootProjects, u._2.buildSettings))

    def structureFrom(s: BuildStructure) = new BuildStructure(
      s.units map mapUnit, s.root, s.settings, s.data,
      s.index, s.streams, s.delegates, s.scopeLocal)

    structureFrom(structure)
  }

}