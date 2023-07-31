package com.colisweb.gdrive.client

import com.colisweb.gdrive.client.drive.{CsvFileType, ExcelSpreadsheetType, GoogleDriveClient, GoogleSearchResult}
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.io.File
import java.nio.file.Paths

class GoogleDriveClientTest extends AnyFlatSpec with Matchers {
  val authenticator = GoogleAuthenticator.fromResource("google-credentials.json", Some("RoutingAnalysis"))
  val drive         = new GoogleDriveClient(authenticator)
  val csvFile       = getFile("test_drive.csv")
  val excelFile     = getFile("test_drive.xlsx")

  it should "test listing files after upload a file in a new folder" in {
    val folderId    = drive.createFolder("folder_name")
    val csvFileId   = drive.uploadFile(folderId, csvFile, "csvFile", CsvFileType, None)
    val excelFileId = drive.uploadFile(folderId, excelFile, "excelFile", ExcelSpreadsheetType, None)

    val l = drive.listFilesInFolder(folderId)

    l should contain theSameElementsAs List(
      GoogleSearchResult(excelFileId, "excelFile"),
      GoogleSearchResult(csvFileId, "csvFile")
    )

    drive.delete(csvFileId)
    drive.delete(excelFileId)
    drive.delete(folderId)
  }

  it should "upload a file in a shared drive" in {
    val sharedDriveFolderId = "1SbEskN8Sjf7ScEPMU__th0Y0JLe-mAK_"
    val folderId            = drive.createFolder("can be safely deleted", Some(sharedDriveFolderId))
    val csvFileId           = drive.uploadFile(folderId, csvFile, "csvFile", CsvFileType, None)

    val l = drive.listFilesInFolder(folderId)

    l should contain theSameElementsAs List(
      GoogleSearchResult(csvFileId, "csvFile")
    )
    // Note : this test keeps creating files here https://drive.google.com/drive/folders/1SbEskN8Sjf7ScEPMU__th0Y0JLe-mAK_?usp=drive_link
    // I did not manage to remove the files automatically but the drawback does not seem too bad.
  }

  it should "test uploading a folder in a non-existing parent" in {
    a[GoogleJsonResponseException] should be thrownBy drive.createFolder("folder_name", Some("non-existing-id"))
  }

  def getFile(fileName: String): File = Paths.get(s"src/test/resources/com/colisweb/gdrive/client/$fileName").toFile
}
