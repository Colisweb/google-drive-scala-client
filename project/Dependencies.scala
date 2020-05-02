import sbt._

object GoogleDrive {
  final val client = "com.google.api-client"   % "google-api-client"          % "1.30.9"
  final val oauth  = "com.google.oauth-client" % "google-oauth-client"        % "1.30.6"
  final val sheets = "com.google.apis"         % "google-api-services-sheets" % "v4-rev20200424-1.30.9"
  final val drive  = "com.google.apis"         % "google-api-services-drive"  % "v3-rev20200413-1.30.9"

  final val all = Seq(client, sheets, oauth, drive)
}

object Cats {
  final val catsEffect = "org.typelevel"    %% "cats-effect" % "2.1.3"
  final val catsRetry  = "com.github.cb372" %% "cats-retry"  % "1.1.0"

  final val all = Seq(catsEffect, catsRetry)
}

object TestDependencies {
  val scalaTest = "org.scalatest" %% "scalatest" % "3.1.1" % Test
}
