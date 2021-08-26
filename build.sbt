import CompileFlags._

lazy val scala212               = "2.12.14"
lazy val scala213               = "2.13.6"
lazy val supportedScalaVersions = List(scala213, scala212)

ThisBuild / organization := "com.colisweb"
ThisBuild / scalaVersion := scala213
ThisBuild / scalafmtOnCompile := true
ThisBuild / scalafmtCheck := true
ThisBuild / scalafmtSbtCheck := true
ThisBuild / crossScalaVersions := supportedScalaVersions
ThisBuild / scalacOptions ++= crossScalacOptions(scalaVersion.value)

Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / pushRemoteCacheTo := Some(
  MavenCache("local-cache", baseDirectory.value / sys.env.getOrElse("CACHE_PATH", "sbt-cache"))
)


//// Main projects

lazy val root = Project(id = "google-drive-scala", base = file("."))
  .settings(noPublishSettings: _*)
  .aggregate(core, cats_wrapper, zio_wrapper)

lazy val core = Project(id = "google-drive-scala-client", base = file("core"))
  .settings(
    libraryDependencies ++= Seq(
      Dependencies.scalaCompat,
      Dependencies.googleClient,
      Dependencies.googleAuth,
      Dependencies.googleCredentials,
      Dependencies.googleSheets,
      Dependencies.googleDrive
    ),
    libraryDependencies += TestDependencies.scalaTest,
  )
  .settings(
    unusedCompileDependenciesFilter -= moduleFilter("org.scala-lang.modules","scala-collection-compat")
  )

lazy val cats_wrapper = Project(id = "google-drive-scala-client-cats", base = file("cats_wrapper"))
  .settings(
    libraryDependencies ++= Seq(
      Dependencies.catsEffect,
      Dependencies.catsRetry
    )
  )
  .dependsOn(core)

lazy val zio_wrapper = Project(id = "google-drive-scala-client-zio", base = file("zio_wrapper"))
  .settings(
    libraryDependencies ++= Seq(
      Dependencies.zio
    )
  )
  .dependsOn(core)

def noPublishSettings =
  Seq(
    publish := {},
    publishLocal := {},
    publishArtifact := false
  )
