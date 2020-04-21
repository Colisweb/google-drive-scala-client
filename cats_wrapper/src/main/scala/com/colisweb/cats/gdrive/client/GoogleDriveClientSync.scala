package com.colisweb.cats.gdrive.client

import java.io.File

import cats.effect.{Sync, Timer}
import com.colisweb.gdrive.client.GoogleDriveRole.GoogleDriveRole
import com.colisweb.gdrive.client.{GoogleAuthenticator, GoogleDriveClient, GoogleMimeType, GoogleSearchResult}
import com.google.api.services.drive.model.Permission
import retry._

class GoogleDriveClientSync[F[_]](authenticator: GoogleAuthenticator, retryPolicy: RetryPolicy[F])(
    implicit F: Sync[F],
    timer: Timer[F]
) extends Retry[F](retryPolicy) {

  val client = new GoogleDriveClient(authenticator)

  def uploadFileTo(
      folderId: String,
      file: File,
      driveFilename: String,
      filetype: GoogleMimeType
  ): F[String] =
    retry(
      client.uploadFileTo(folderId, file, driveFilename, filetype)
    )

  def createFolderTo(parentId: String, name: String): F[String] =
    retry(
      client.createFolderTo(parentId, name)
    )

  def delete(fileId: String): F[Unit] =
    retry(
      client.delete(fileId)
    )

  def listFilesInFolder(folderId: String): F[List[GoogleSearchResult]] =
    retry(
      client.listFilesInFolder(folderId)
    )

  def uploadFile(file: File, driveFilename: String, filetype: GoogleMimeType): F[String] =
    retry(
      client.uploadFile(file, driveFilename, filetype)
    )

  def createFolder(name: String): F[String] =
    retry(
      client.createFolder(name)
    )

  def move(targetId: String, parentId: String): F[Unit] =
    retry(
      client.move(targetId, parentId)
    )

  def share(fileId: String, email: String, role: GoogleDriveRole): F[Permission] =
    retry(
      client.share(fileId, email, role)
    )
}
