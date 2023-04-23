import org.scalajs.linker.interface.ModuleSplitStyle


val slinkyVersion = "0.7.3"


lazy val contentProject = ProjectRef(file("../online-test-suite"), "fooJS")

lazy val basicLibs: Project => Project = _.settings(
  libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "2.4.0",
  libraryDependencies += "me.shadaj" %%% "slinky-core" % slinkyVersion, // core React functionality, no React DOM
  libraryDependencies += "me.shadaj" %%% "slinky-web" % slinkyVersion, // React DOM, HTML and SVG tags
  libraryDependencies += "me.shadaj" %%% "slinky-native" % slinkyVersion, // React Native components
  libraryDependencies += "me.shadaj" %%% "slinky-hot" % slinkyVersion, // Hot loading, requires react-proxy package
  //libraryDependencies += "me.shadaj" %%% "slinky-scalajsreact-interop" % slinkyVersion // Interop with japgolly/scalajs-react
)


lazy val baseSettings: Project => Project =
  _.enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin, ScalablyTypedConverterPlugin)
    .settings(
      version := "0.2",
      name := "testerUI",
      scalaVersion := "2.13.10",
    //scalacOptions ++= ScalacOptions.flags,
      scalaJSUseMainModuleInitializer := true,
      /* disabled because it somehow triggers many warnings */
      scalaJSLinkerConfig := scalaJSLinkerConfig.value.withSourceMap(false),
      /* for slinky */
      scalacOptions += "-Ymacro-annotations",
      stFlavour := Flavour.Slinky,
      useYarn := true,
      webpackDevServerPort := 8006,
    ).dependsOn(contentProject)

lazy val bundlerSettings: Project => Project =
  _.enablePlugins()

// specify versions for all of reacts dependencies to compile less since we have many demos here
lazy val npmDeps: Project => Project =
  _.settings(
    stTypescriptVersion := "3.9.3",
    Compile / npmDependencies ++= Seq(
      "react" -> "16.13.1",
      "react-dom" -> "16.13.1",
      "@types/react" -> "16.9.42",
      "@types/react-dom" -> "16.9.8",
      "csstype" -> "2.6.11",
      "@types/prop-types" -> "15.7.3",
      "antd" -> "4.9.4"
    )
  )


// Define task to  copy html files
lazy val copyJS = taskKey[Unit]("Copy js")

// Implement task
copyJS := {
  java.nio.file.Files.copy(
    new File("/home/azu/projects/testerUI/target/scala-2.13/scalajs-bundler/main/testerui-fastopt-bundle.js").toPath,
    new File("/home/azu/projects/testerUI/data/testerui-fastopt-bundle.js").toPath,
    java.nio.file.StandardCopyOption.REPLACE_EXISTING
  )

}




lazy val root = (project in file(".")).configure(baseSettings, basicLibs, npmDeps)