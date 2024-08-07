import com.typesafe.sbt.packager.docker.*
import sbtghactions.JavaSpec

ThisBuild / scalaVersion                        := "3.4.1"
ThisBuild / version                             := scala.sys.process.Process("git rev-parse HEAD").!!.trim.slice(0, 7)
ThisBuild / organization                        := "io.github.kirill5k"
ThisBuild / githubWorkflowPublishTargetBranches := Nil
ThisBuild / githubWorkflowJavaVersions          := Seq(JavaSpec.temurin("22"))
ThisBuild / scalacOptions ++= Seq("-Wunused:all")

val noPublish = Seq(
  publish         := {},
  publishLocal    := {},
  publishArtifact := false,
  publish / skip  := true
)

val docker = Seq(
  packageName        := moduleName.value,
  version            := version.value,
  maintainer         := "immotional@aol.com",
  dockerBaseImage    := "amazoncorretto:22.0.0-alpine",
  dockerUpdateLatest := true,
  dockerUsername     := sys.env.get("DOCKER_USERNAME"),
  dockerRepository   := sys.env.get("DOCKER_REPO_URI"),
  makeBatScripts     := Nil,
  dockerEnvVars ++= Map("VERSION" -> version.value),
  dockerCommands := {
    val commands         = dockerCommands.value
    val (stage0, stage1) = commands.span(_ != DockerStageBreak)
    val (before, after)  = stage1.splitAt(4)
    val installBash      = Cmd("RUN", "apk update && apk upgrade && apk add bash")
    stage0 ++ before ++ List(installBash) ++ after
  }
)

val core = project
  .in(file("modules/core"))
  .enablePlugins(JavaAppPackaging, JavaAgent, DockerPlugin)
  .settings(docker)
  .settings(
    name                 := "stocks-checker-core",
    moduleName           := "stocks-checker-core",
    libraryDependencies ++= Dependencies.core ++ Dependencies.test
  )

val root = project
  .in(file("."))
  .settings(noPublish)
  .settings(
    name := "stocks-checker"
  )
  .aggregate(core)