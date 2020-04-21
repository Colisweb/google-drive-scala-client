package com.colisweb.gdrive.client

import java.io.{File, PrintWriter}

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class GoogleDriveClientTest extends AnyFlatSpec with Matchers {

  it should "upload and delete file in a folder to drive" in {

    val authenticator = GoogleAuthenticator("google-credentials.json", "RoutingAnalysis")
    val drive         = new GoogleDriveClient(authenticator)

    val file = new File("/tmp/file")
    val pw   = new PrintWriter(file)
    pw.write("title; date")
    pw.close()

    val folderId = drive.createFolder("folder_name")
    val fileId   = drive.uploadFileTo(folderId, file, "filename", CsvFileType)

    file.delete()
    drive.delete(fileId)
    drive.delete(folderId)
  }

}
