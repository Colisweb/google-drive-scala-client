import sbt._

object TestVersions {
  final val scalaTest = "3.2.15"
  final val approvals = "1.3.1"
}

object TestDependencies {
  final val scalaTest = "org.scalatest" %% "scalatest"       % TestVersions.scalaTest % Test
  final val approvals = "com.colisweb"  %% "approvals-scala" % TestVersions.approvals % Test
}

object Versions {
  final val catsEffect     = "2.5.5"
  final val catsRetry      = "2.1.1"
  final val circe          = "0.14.5"
  final val scalaCompat    = "2.9.0"
  final val scalaReflect   = "2.13.10"
  final val googleClient   = "2.2.0"
  final val googleAuth     = "1.16.0"
  final val googleSheets   = "v4-rev20221216-2.0.0"
  final val googleDrive    = "v3-rev20230212-2.0.0"
  final val googleBigQuery = "2.23.1"
  final val zio            = "2.0.10"
}

object Dependencies {
  final val catsEffect        = "org.typelevel"          %% "cats-effect"                     % Versions.catsEffect
  final val catsRetry         = "com.github.cb372"       %% "cats-retry"                      % Versions.catsRetry
  final val circe             = "io.circe"               %% "circe-core"                      % Versions.circe
  final val scalaCompat       = "org.scala-lang.modules" %% "scala-collection-compat"         % Versions.scalaCompat
  final val scalaReflect      = "org.scala-lang"          % "scala-reflect"                   % Versions.scalaReflect
  final val googleClient      = "com.google.api-client"   % "google-api-client"               % Versions.googleClient
  final val googleAuth        = "com.google.auth"         % "google-auth-library-oauth2-http" % Versions.googleAuth
  final val googleCredentials = "com.google.auth"         % "google-auth-library-credentials" % Versions.googleAuth
  final val googleSheets      = "com.google.apis"         % "google-api-services-sheets"      % Versions.googleSheets
  final val googleDrive       = "com.google.apis"         % "google-api-services-drive"       % Versions.googleDrive
  final val googleBigQuery    = "com.google.cloud"        % "google-cloud-bigquery"           % Versions.googleBigQuery
  final val zio               = "dev.zio"                %% "zio"                             % Versions.zio

}
