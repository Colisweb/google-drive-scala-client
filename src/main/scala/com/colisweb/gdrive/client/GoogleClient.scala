package com.colisweb.gdrive.client

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.sheets.v4.{Sheets, SheetsScopes}

import scala.collection.JavaConverters._

/**
  * Authenticates to Google API.
  *
  * @param credentialsPath path to a JSON file in resources
  * @param applicationName registered name
  */
case class GoogleClient(credentialsPath: String, applicationName: String) {

  private lazy val jsonFactory   = JacksonFactory.getDefaultInstance
  private lazy val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
  private val scopes             = List(SheetsScopes.DRIVE)

  // FIXME: update deprecated login flow
  private lazy val credentials: Credential =
    GoogleCredential
      .fromStream(getClass.getClassLoader.getResourceAsStream(credentialsPath))
      .createScoped(scopes.asJavaCollection)

  def sheetsClient(sheetId: String): GoogleSheetsClient = {
    val sheets = new Sheets.Builder(httpTransport, jsonFactory, credentials)
      .setApplicationName(applicationName)
      .build()
    GoogleSheetsClient(sheets, sheetId)
  }

  def driveClient: GoogleDriveApiClient = {
    val drive = new Drive.Builder(httpTransport, jsonFactory, credentials)
      .setApplicationName(applicationName)
      .build()
    GoogleDriveApiClient(drive)
  }
}
