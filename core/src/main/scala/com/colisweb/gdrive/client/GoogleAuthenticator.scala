package com.colisweb.gdrive.client

import java.io.{FileInputStream, InputStream}

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.sheets.v4.SheetsScopes

import scala.collection.JavaConverters._

/**
  * Authenticates to Google API.
  *
  * @param credentialsInputStream path to a JSON file in resources
  * @param applicationName registered name
  */
case class GoogleAuthenticator(credentialsInputStream: InputStream, applicationName: String) {

  lazy val jsonFactory: JacksonFactory     = JacksonFactory.getDefaultInstance
  lazy val httpTransport: NetHttpTransport = GoogleNetHttpTransport.newTrustedTransport()
  private val scopes                       = List(SheetsScopes.DRIVE)

  // FIXME: update deprecated login flow
  lazy val credentials: Credential =
    GoogleCredential
      .fromStream(credentialsInputStream)
      .createScoped(scopes.asJavaCollection)
}

object GoogleAuthenticator {

  def fromFile(path: String, applicationName: String): GoogleAuthenticator =
    GoogleAuthenticator(
      credentialsInputStream = new FileInputStream(path),
      applicationName = applicationName
    )

  def fromResource(path: String, applicationName: String): GoogleAuthenticator =
    GoogleAuthenticator(
      credentialsInputStream = getClass.getClassLoader.getResourceAsStream(path),
      applicationName = applicationName
    )
}
