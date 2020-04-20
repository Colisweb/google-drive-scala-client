package com.colisweb.gdrive.client

sealed trait GoogleMimeType
case object GoogleSpreadsheet extends GoogleMimeType
case object GoogleDriveFolder extends GoogleMimeType
case object CsvFile           extends GoogleMimeType

object GoogleMimeType {
  val driveFolder = "application/vnd.google-apps.folder"
  val spreadsheet = "application/vnd.google-apps.spreadsheet"
  val csvFile     = "text/csv"

  def name(googleMimeType: GoogleMimeType): String =
    googleMimeType match {
      case GoogleDriveFolder => driveFolder
      case GoogleSpreadsheet => spreadsheet
      case CsvFile           => csvFile
    }
}
