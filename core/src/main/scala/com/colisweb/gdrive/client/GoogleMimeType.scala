package com.colisweb.gdrive.client

sealed trait GoogleMimeType
case object GoogleSpreadsheetType extends GoogleMimeType
case object GoogleDriveFolderType extends GoogleMimeType
case object CsvFileType           extends GoogleMimeType

object GoogleMimeType {
  val driveFolder = "application/vnd.google-apps.folder"
  val spreadsheet = "application/vnd.google-apps.spreadsheet"
  val csvFile     = "text/csv"

  def name(googleMimeType: GoogleMimeType): String =
    googleMimeType match {
      case GoogleDriveFolderType => driveFolder
      case GoogleSpreadsheetType => spreadsheet
      case CsvFileType           => csvFile
    }
}
