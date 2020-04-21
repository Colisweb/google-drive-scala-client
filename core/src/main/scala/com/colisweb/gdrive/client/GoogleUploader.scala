package com.colisweb.gdrive.client

import java.io.File

class GoogleUploader(driveClient: GoogleDriveClient) {

  def uploadDirectoryTo(parentId: String, destinationPath: List[String], localFolder: File): String = {
    val files    = localFolder.listFiles.filter(_.isFile).toList
    val folderId = uploadPathTo(parentId, destinationPath)
    uploadFilesTo(folderId, files)
    folderId
  }

  private def uploadPathTo(parentId: String, path: List[String]): String =
    path.fold(parentId)(findOrCreateFolder)

  private def uploadFilesTo(folderId: String, files: List[File]): Unit =
    files.foreach(file => driveClient.uploadFileTo(file.getName, file, folderId, CsvFileType))

  private def findOrCreateFolder(parentId: String, folderName: String): String =
    driveClient
      .listFilesInFolder(parentId)
      .find(_.name == folderName)
      .fold(driveClient.createFolderTo(parentId, folderName))(_.id)

}
