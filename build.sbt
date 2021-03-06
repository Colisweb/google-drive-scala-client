import CompileFlags._

lazy val scala212               = "2.12.11"
lazy val scala213               = "2.13.2"
lazy val supportedScalaVersions = List(scala213, scala212)

ThisBuild / organization := "com.colisweb"
ThisBuild / scalaVersion := scala213
ThisBuild / scalafmtOnCompile := true
ThisBuild / scalafmtCheck := true
ThisBuild / scalafmtSbtCheck := true
ThisBuild / crossScalaVersions := supportedScalaVersions
ThisBuild / scalacOptions ++= crossScalacOptions(scalaVersion.value)

Global / onChangedBuildSource := ReloadOnSourceChanges

//// Main projects

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

ThisBuild / licenses := Seq("MIT" -> url("http://opensource.org/licenses/MIT"))
ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://gitlab.com/colisweb-open-source/scala/google-drive-scala-client"),
    "scm:git:git@gitlab.com:colisweb-open-source/scala/google-drive-scala-client.git"
  )
)

ThisBuild / bintrayOrganization := Some("colisweb")
ThisBuild / publishMavenStyle := true
