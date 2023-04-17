package com.colisweb.gdrive.client

import com.colisweb.gdrive.client.drive.{CsvFileType, ExcelSpreadsheetType, GoogleDriveClient, GoogleSearchResult}
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.io.File
import java.nio.file.Paths

class GoogleDriveClientTest extends AnyFlatSpec with Matchers {

  it should "test listing files after upload a file in a new folder" in {

    val authenticator = GoogleAuthenticator.fromResource("google-credentials.json", Some("RoutingAnalysis"))
    val drive         = new GoogleDriveClient(authenticator)

    val csvFile   = getFile("test_drive.csv")
    val excelFile = getFile("test_drive.csv")

    val folderId    = drive.createFolder("folder_name")
    val csvFileId   = drive.uploadFileTo(folderId, csvFile, "csvFile", CsvFileType, None)
    val excelFileId = drive.uploadFileTo(folderId, excelFile, "excelFile", ExcelSpreadsheetType, None)

    val l = drive.listFilesInFolder(folderId)

    l should contain theSameElementsAs List(
      GoogleSearchResult(excelFileId, "excelFile"),
      GoogleSearchResult(csvFileId, "csvFile")
    )

    drive.delete(csvFileId)
    drive.delete(excelFileId)
    drive.delete(folderId)
  }

  it should "test uploading a folder in a non-existing parent" in {

    val authenticator = GoogleAuthenticator.fromResource("google-credentials.json", Some("RoutingAnalysis"))
    val drive         = new GoogleDriveClient(authenticator)

    a[GoogleJsonResponseException] should be thrownBy drive.createFolderTo("non-existing-id", "folder_name")
  }

  def getFile(fileName: String): File = Paths.get(s"src/test/resources/com/colisweb/gdrive/client/$fileName").toFile
}
