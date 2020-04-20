package com.colisweb.cats.gdrive.client

import java.io.File

import cats.effect.Sync
import com.colisweb.gdrive.client.GoogleDriveRole.GoogleDriveRole
import com.colisweb.gdrive.client.{GoogleAuthenticator, GoogleDriveClient, GoogleSearchResult}
import com.google.api.services.drive.model.Permission

class GoogleDriveClientSync[F[_]](authenticator: GoogleAuthenticator)(implicit F: Sync[F]) {

  val client = new GoogleDriveClient(authenticator)

  def uploadTo(
      driveFilename: String,
      fileToUpload: File,
      destinationFolderId: String,
      targetMimeType: String,
      outputMimeType: Option[String]
  ): F[Unit] =
    F.delay(
      client.uploadTo(driveFilename, fileToUpload, destinationFolderId, targetMimeType, outputMimeType)
    )

  def createFolderTo(parentId: String, name: String): F[String] =
    F.delay(
      client.createFolderTo(parentId, name)
    )

  def delete(fileId: String): F[String] =
    F.delay(
      client.delete(fileId)
    )

  def listFilesInFolder(folderId: String): F[List[GoogleSearchResult]] =
    F.delay(
      client.listFilesInFolder(folderId)
    )

  def move(targetId: String, parentId: String): F[Unit] =
    F.delay(
      client.move(targetId, parentId)
    )

  def share(fileId: String, email: String, role: GoogleDriveRole): F[Permission] =
    F.delay(
      client.share(fileId, email, role)
    )
}
