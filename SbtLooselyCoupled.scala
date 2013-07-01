import sbt._
import sbt.Keys._
import sbt.Load._

object SbtLooselyCoupled extends Plugin {

  def addPlugins(plugins: File*) = addPluginsFor(_ => true, plugins: _*)

  def addPluginsFor(add: BuildLoader.BuildInfo => Boolean,
                    plugins: File*) = BuildLoader.build {
    b => if (!add(b)) None
    else {

      def addResolver(config: LoadBuildConfiguration) =
        config.globalPlugin.map {
          p => config.copy(globalPlugin = Some(p.copy(
            inject = p.inject ++ Seq(internalResolver))))
        }.getOrElse(config)

      Some(() => loadUnit(b.uri, b.base, b.state,
        plugins.foldLeft(b.config) {
          (config, plugin) => addResolver(loadGlobal(b.state,
            b.base, plugin.getAbsoluteFile, config))
        }))

    }
  }

  def internalResolver = projectResolver <<= projectDescriptors map {
    m => new RawRepository(new ProjectResolver("loosely-coupled-resolver", m))
  }

  type ProjectLinker = (BuildDependencies,
    Seq[ModuleID], ProjectRef, String) => BuildDependencies

  def linkBuilds = buildsLinker(linkProject)

  def buildsLinker(linker: ProjectLinker) =
    addSettings(inProjects = Seq(internalResolver,
      buildDependencies in Global <<= (buildDependencies in Global,
        libraryDependencies, thisProjectRef, organization)(linker)),
      inBuilds = Seq(onLoad in Global := onLoadBuild))

  def onLoadBuild(state: State) = state get stateBuildStructure flatMap {
    structure => (buildDependencies in Global) get structure.data map {
      dependencies => state.copy(attributes = state.attributes.put(
        stateBuildStructure, linkBuildStructure(structure, dependencies)))
    }
  } getOrElse state

  def newBuild(ss: Seq[Setting[_]], ps: Seq[Project],
               ls: Seq[BuildLoader.Components]): Build =
    new Build {
      override def buildLoaders = ls

      override def settings = ss

      override def projects = ps
    }

  def newBuild(b: Build)(
    settings: Seq[Setting[_]] = b.settings, projects: Seq[Project] = b.projects,
    buildLoaders: Seq[BuildLoader.Components] = b.buildLoaders): Build =
    newBuild(settings, projects, buildLoaders)

  def addSettings(inBuilds: Seq[Setting[_]] = Seq(),
                  inProjects: Seq[Setting[_]] = Seq(),
                  addLoaders: Seq[BuildLoader.Components] = Seq()) = {

    def buildFrom(b: Build) = newBuild(b.settings ++ inBuilds,
      b.projects map (_.settings(inProjects: _*)), b.buildLoaders ++ addLoaders)

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

    def classpath(uri: URI, id: String) = dependencies.
      classpath.getOrElse(ProjectRef(uri, id), Seq())

    def toResolved(cp: ClasspathDep[ProjectRef]) =
      ResolvedClasspathDependency(cp.project, cp.configuration)

    def unitFrom(u: BuildUnit) = {

      def depends(p: Project) = classpath(u.uri, p.id).
        foldLeft(p)((p, d) => p.dependsOn(d.project))

      def buildFrom(b: Build) = newBuild(b)(
        projects = b.projects map depends)

      def defsFrom(d: LoadedDefinitions) = new LoadedDefinitions(
        d.base, d.target, d.loader, d.builds map buildFrom, d.buildNames)

      new BuildUnit(u.uri, u.localBase, defsFrom(u.definitions), u.plugins)
    }

    def definedFrom(uri: URI)(defined: (String, ResolvedProject)) = {
      val (u, p) = defined
      val resolved = Project.resolved(p.id, p.base, p.aggregate,
        (p.dependencies map toResolved) ++ (classpath(uri, p.id) map toResolved),
        p.delegates, p.settings, p.configurations)
      (u, resolved)
    }

    def mapUnit(unit: (URI, LoadedBuildUnit)) = {
      val (uri, u) = unit
      val loaded = new LoadedBuildUnit(unitFrom(u.unit),
        u.defined map definedFrom(uri), u.rootProjects, u.buildSettings)
      (uri, loaded)
    }

    def structureFrom(s: BuildStructure) = new BuildStructure(
      s.units map mapUnit, s.root, s.settings, s.data,
      s.index, s.streams, s.delegates, s.scopeLocal)

    structureFrom(structure)
  }

}