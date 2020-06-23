package com.colisweb.cats.gdrive.client

import java.io.File

import cats.effect.{Sync, Timer}
import cats.implicits._
import com.colisweb.gdrive.client.GoogleDriveRole.GoogleDriveRole
import com.colisweb.gdrive.client.{GoogleAuthenticator, GoogleDriveClient, GoogleMimeType, GoogleSearchResult}
import com.google.api.services.drive.model.Permission
import retry._

class GoogleDriveClientSync[F[_]](
    authenticator: GoogleAuthenticator,
    retryPolicy: RetryPolicy[F],
    onError: (Throwable, RetryDetails) => F[Unit]
)(implicit
    timer: Timer[F],
    S: Sync[F]
) extends Retry[F](retryPolicy, onError) {

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
    ) *> S.unit

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
    ) *> S.unit

  def share(fileId: String, email: String, role: GoogleDriveRole): F[Permission] =
    retry(
      client.share(fileId, email, role)
    )

  def getParents(id: String): F[List[String]] =
    retry(
      client.getParents(id)
    )
}

object GoogleDriveClientSync {

  def apply[F[_]: Sync](
      authenticator: GoogleAuthenticator
  )(implicit timer: Timer[F]): GoogleDriveClientSync[F] =
    new GoogleDriveClientSync(authenticator, Retry.defaultPolicy[F], Retry.defaultOnError[F])
}
