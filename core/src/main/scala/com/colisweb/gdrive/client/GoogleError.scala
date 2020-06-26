package com.colisweb.gdrive.client

sealed trait GoogleError extends Exception

final case class FolderNotFound(keywords: String) extends GoogleError

final case class SpreadsheetNotFound(keywords: String) extends GoogleError

final case class CsvFileNotFound(keywords: String) extends GoogleError

final case class FileNotFound(keywords: String) extends GoogleError
