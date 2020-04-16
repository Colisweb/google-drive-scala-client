package com.colisweb.gdrive.client

import java.io.File

import cats.effect.Sync
import cats.implicits._

class GoogleUploader[F[_]](driveClient: GoogleDriveApiClient[F])(implicit F: Sync[F]) {

  def uploadDirectoryTo(parentId: String, destinationPath: List[String], localFolder: File): F[String] = {

    for {
      files    <- F.delay(localFolder.listFiles.filter(_.isFile).toList)
      folderId <- uploadPathTo(parentId, destinationPath)
      _        <- uploadFilesTo(folderId, files)
    } yield folderId
  }

  private def uploadPathTo(parentId: String, path: List[String]): F[String] =
    path.foldM(parentId)(findOrCreateFolder)

  private def uploadFilesTo(folderId: String, files: List[File]): F[Unit] =
    files.traverse_(file =>
      driveClient.uploadTo(file.getName, file, folderId, GoogleMimeType.csvFile, Some(GoogleMimeType.spreadsheet))
    )

  private def findOrCreateFolder(parentId: String, folderName: String): F[String] =
    driveClient
      .listFilesInFolder(parentId)
      .map(searchResults => searchResults.find(_.name == folderName))
      .flatMap {
        case None                            => driveClient.createFolderTo(parentId, folderName)
        case Some(GoogleSearchResult(id, _)) => F.pure(id)
      }

}
