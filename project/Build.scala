import sbt._
import Keys._
import pl.project13.scala.sbt.JmhPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin
//import ScalaJSPlugin._
import ScalaJSPlugin.autoImport._
import Lib._

object ScalaJsBenchmark extends Build {

  val Scala211 = "2.11.7"

  def scalacFlags = Seq(
    "-deprecation", "-unchecked", "-feature",
    "-language:postfixOps", "-language:implicitConversions", "-language:higherKinds", "-language:existentials")

  val commonSettings: PE =
    _.settings(
      organization             := "com.github.japgolly.tempname",
      version                  := "0.1.0-SNAPSHOT",
      homepage                 := Some(url("https://github.com/japgolly/tempname")),
      licenses                 += ("Apache-2.0", url("http://opensource.org/licenses/Apache-2.0")),
      scalaVersion             := Scala211,
      scalacOptions           ++= scalacFlags,
      clearScreenTask          := clearScreen(),
      shellPrompt in ThisBuild := ((s: State) => Project.extract(s).currentRef.project + "> "),
      incOptions               := incOptions.value.withNameHashing(true),
      updateOptions            := updateOptions.value.withCachedResolution(true))
    .configure(
      addCommandAliases(
        "/"    -> "project root",
        "C"    -> "root/clean",
        "cc"   -> ";clear;compile",
        "ctc"  -> ";clear;test:compile",
        "ct"   -> ";clear;test",
        "cq"   -> ";clear;testQuick",
        "ccc"  -> ";clear;clean;compile",
        "cctc" -> ";clear;clean;test:compile",
        "cct"  -> ";clear;clean;test"))

  override def rootProject = Some(root)

  lazy val root =
    Project("root", file("."))
      .configure(commonSettings)
      .aggregate(benchmark, demo)

  lazy val benchmark =
    Project("benchmark", file("benchmark"))
      .enablePlugins(ScalaJSPlugin)
      .configure(commonSettings)
      .settings(
        libraryDependencies ++= Seq(
          "com.github.japgolly.scalajs-react" %%% "core"  % "0.10.1",
          "com.github.japgolly.scalajs-react" %%% "extra" % "0.10.1"),
        jsDependencies ++= Seq(
          "org.webjars.npm" % "react"     % "0.14.2" / "react-with-addons.js" commonJSName "React"    minified "react-with-addons.min.js",
          "org.webjars.npm" % "react-dom" % "0.14.2" / "react-dom.js"         commonJSName "ReactDOM" minified "react-dom.min.js" dependsOn "react-with-addons.js")
      )

  val demoJs = "output.js"
  lazy val demo =
    Project("demo", file("demo"))
      .enablePlugins(ScalaJSPlugin)
      .configure(commonSettings)
      .dependsOn(benchmark)
      .settings(
        artifactPath in (Compile, fastOptJS) := ((target in Compile).value / demoJs),
        artifactPath in (Compile, fullOptJS) := ((target in Compile).value / demoJs))
}