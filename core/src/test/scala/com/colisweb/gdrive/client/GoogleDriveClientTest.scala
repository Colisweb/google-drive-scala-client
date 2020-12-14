package com.colisweb.gdrive.client

import java.io.{File, PrintWriter}

import com.colisweb.gdrive.client.drive.{CsvFileType, GoogleDriveClient, GoogleSearchResult}
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class GoogleDriveClientTest extends AnyFlatSpec with Matchers {

  it should "test listing files after upload a file in a new folder" in {

    val authenticator = GoogleAuthenticator.fromResource("google-credentials.json", "RoutingAnalysis")
    val drive         = new GoogleDriveClient(authenticator)

    val file = new File("/tmp/file")
    val pw   = new PrintWriter(file)
    pw.write("title; date")
    pw.close()

    val folderId = drive.createFolder("folder_name")
    val fileId   = drive.uploadFileTo(folderId, file, "filename", CsvFileType, None)

    val l = drive.listFilesInFolder(folderId)

    l.head should be(GoogleSearchResult(fileId, "filename"))

    file.delete()
    drive.delete(fileId)
    drive.delete(folderId)
  }

  it should "test uploading a folder in a non-existing parent" in {

    val authenticator = GoogleAuthenticator.fromResource("google-credentials.json", "RoutingAnalysis")
    val drive         = new GoogleDriveClient(authenticator)

    a[GoogleJsonResponseException] should be thrownBy drive.createFolderTo("non-existing-id", "folder_name")
  }

}
