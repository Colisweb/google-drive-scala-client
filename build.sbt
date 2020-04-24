ThisBuild / organization := "com.colisweb"
ThisBuild / scalaVersion := "2.12.11"
ThisBuild / scalafmtOnCompile := true
ThisBuild / scalafmtCheck := true
ThisBuild / scalafmtSbtCheck := true

Global / onChangedBuildSource := ReloadOnSourceChanges

//// Main projects

lazy val core = Project(id = "google-drive-scala-client", base = file("core"))
  .settings(
    libraryDependencies ++= GoogleDrive.all,
    libraryDependencies += TestDependencies.scalaTest
  )

lazy val cats_wrapper = Project(id = "google-drive-scala-client-cats", base = file("cats_wrapper"))
  .settings(
    libraryDependencies ++= Cats.all,
    libraryDependencies += TestDependencies.scalaTest
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
