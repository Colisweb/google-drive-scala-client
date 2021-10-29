import sbt._

object TestDependencies {
  val scalaTest = "org.scalatest" %% "scalatest" % "3.2.10" % Test
}

object Versions {
  final val catsEffect   = "3.2.9"
  final val catsRetry    = "2.1.1"
  final val scalaCompat  = "2.5.0"
  final val googleClient = "1.32.2"
  final val googleAuth   = "1.2.2"
  final val googleSheets = "v4-rev20210629-1.32.1"
  final val googleDrive  = "v3-rev20211017-1.32.1"
  final val zio          = "1.0.12"
}

object Dependencies {
  final val catsEffect        = "org.typelevel"          %% "cats-effect"                     % Versions.catsEffect
  final val catsRetry         = "com.github.cb372"       %% "cats-retry"                      % Versions.catsRetry
  final val scalaCompat       = "org.scala-lang.modules" %% "scala-collection-compat"         % Versions.scalaCompat
  final val googleClient      = "com.google.api-client"   % "google-api-client"               % Versions.googleClient
  final val googleAuth        = "com.google.auth"         % "google-auth-library-oauth2-http" % Versions.googleAuth
  final val googleCredentials = "com.google.auth"         % "google-auth-library-credentials" % Versions.googleAuth
  final val googleSheets      = "com.google.apis"         % "google-api-services-sheets"      % Versions.googleSheets
  final val googleDrive       = "com.google.apis"         % "google-api-services-drive"       % Versions.googleDrive
  final val zio               = "dev.zio"                %% "zio"                             % Versions.zio

}
