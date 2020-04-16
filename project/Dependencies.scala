import sbt._

object GoogleDrive {
  final val client = "com.google.api-client" % "google-api-client" % "1.30.9"
  final val oauth = "com.google.oauth-client" % "google-oauth-client" % "1.30.6"
  final val sheets = "com.google.apis" % "google-api-services-sheets" % "v4-rev20200312-1.30.9"
  final val drive = "com.google.apis" % "google-api-services-drive" % "v3-rev20200326-1.30.9"

  final val all = Seq(client, sheets, oauth, drive)
}

object Cats {
  val cats = "org.typelevel" %% "cats-core" % "2.1.1"
  val catsEffect = "org.typelevel" %% "cats-effect" % "2.1.1"

  final lazy val all = Seq(cats, catsEffect)
}
