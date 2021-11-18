package com.colisweb.gdrive.client

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.auth.oauth2.GoogleCredentials

import java.io.{FileInputStream, InputStream}
import scala.jdk.CollectionConverters._

/** Authenticates to Google API.
  *
  * @param credentialsInputStream
  *   path to a JSON file in resources
  * @param applicationName
  *   registered name
  */
case class GoogleAuthenticator(
    credentialsInputStream: InputStream,
    applicationName: Option[String] = None,
    scopes: List[String] = GoogleAuthenticator.defaultScopes
) {

  lazy val jsonFactory: GsonFactory        = GsonFactory.getDefaultInstance
  lazy val httpTransport: NetHttpTransport = GoogleNetHttpTransport.newTrustedTransport()

  lazy val credentials: GoogleCredentials =
    GoogleCredentials
      .fromStream(credentialsInputStream)
      .createScoped(scopes.asJavaCollection)
}

object GoogleAuthenticator {
  val bigQueryScope = "https://www.googleapis.com/auth/bigquery"
  val driveScope    = SheetsScopes.DRIVE
  val defaultScopes = List(driveScope, bigQueryScope)

  def fromFile(path: String, applicationName: Option[String] = None): GoogleAuthenticator =
    GoogleAuthenticator(
      credentialsInputStream = new FileInputStream(path),
      applicationName = applicationName
    )

  def fromResource(path: String, applicationName: Option[String] = None): GoogleAuthenticator =
    GoogleAuthenticator(
      credentialsInputStream = getClass.getClassLoader.getResourceAsStream(path),
      applicationName = applicationName
    )
}
