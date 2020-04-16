ThisBuild / organization := "com.colisweb"
ThisBuild / scalaVersion := "2.12.11"
ThisBuild / scalafmtOnCompile := true
ThisBuild / scalafmtCheck := true
ThisBuild / scalafmtSbtCheck := true

Global / onChangedBuildSource := ReloadOnSourceChanges


//// Main projects

lazy val root = Project(id = "google-drive-scala-client", base = file("."))
  .settings(libraryDependencies ++= GoogleDrive.all)

ThisBuild / licenses := Seq("MIT" -> url("http://opensource.org/licenses/MIT"))
ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://gitlab.com/colisweb-open-source/scala/google-drive-scala-client"),
    "scm:git:git@gitlab.com:colisweb-open-source/scala/google-drive-scala-client.git"
  )
)

ThisBuild / bintrayOrganization := Some("colisweb")
ThisBuild / publishMavenStyle := true
