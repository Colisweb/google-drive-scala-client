import sbt._

object TestDependencies {
  val scalaTest = "org.scalatest" %% "scalatest" % "3.2.2" % Test
}

object Versions {
  final val catsEffect        = "2.2.0"
  final val catsRetry         = "1.1.1"
  final val scalaCompat       = "2.1.6"
  final val googleClient      = "1.30.9"
  final val googleAuth        = "0.21.1"
  final val googleSheets      = "v4-rev20200616-1.30.9"
  final val googleDrive       = "v3-rev20200618-1.30.9"
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

}
