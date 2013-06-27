import sbt._

object AddPlugin extends Build {

    val samplePlugin = file("../plugin")
    
    lazy val addPlugin = Project(id="add-plugin",
	base=file(".")) dependsOn(samplePlugin)

}