import sbt._

object TestDependencies {
  val scalaTest = "org.scalatest" %% "scalatest" % "3.2.7" % Test
}

object Versions {
  final val catsEffect        = "2.4.1"
  final val catsRetry         = "2.1.0"
  final val scalaCompat       = "2.4.2"
  final val googleClient      = "1.31.4"
  final val googleAuth        = "0.25.3"
  final val googleSheets      = "v4-rev20210322-1.31.0"
  final val googleDrive       = "v3-rev20210524-1.31.0"
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
