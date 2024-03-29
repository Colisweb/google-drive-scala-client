package com.colisweb.cats.gdrive.client

import cats.effect.{Sync, Timer}
import cats.implicits._
import com.colisweb.gdrive.client.GoogleAuthenticator
import com.colisweb.gdrive.client.GoogleUtilities._
import com.colisweb.gdrive.client.drive.GoogleDriveRole.GoogleDriveRole
import com.colisweb.gdrive.client.drive.{GoogleDriveClient, GoogleMimeType, GoogleSearchResult}
import com.google.api.services.drive.model.{FileList, Permission}
import retry.{RetryDetails, RetryPolicy}

import java.io.{File, InputStream}

class GoogleDriveClientRetry[F[_]](
    authenticator: GoogleAuthenticator,
    retryPolicy: RetryPolicy[F],
    onError: (Throwable, RetryDetails) => F[Unit]
)(implicit
    timer: Timer[F],
    S: Sync[F]
) extends Retry[F](retryPolicy, onError) {

  val client = new GoogleDriveClient(authenticator)

  def uploadFile(
      folderId: String,
      file: File,
      driveFilename: String,
      filetype: GoogleMimeType,
      outputFiletype: Option[GoogleMimeType]
  ): F[String] =
    retry(
      client.uploadFile(folderId, file, driveFilename, filetype, outputFiletype)
    )

  def createFolder(name: String, parentId: Option[String] = None): F[String] =
    retry(
      client.createFolder(name, parentId)
    )

  def delete(fileId: String): F[Unit] =
    retry(
      client.delete(fileId)
    ) *> S.unit

  def listFilesInFolder(folderId: String): F[List[GoogleSearchResult]] =
    retry(
      client.listFilesInFolder(folderId)
    )

  def move(targetId: String, parentId: String): F[Unit] =
    retry(
      client.move(targetId, parentId)
    ) *> S.unit

  def share(
      fileId: String,
      email: String,
      role: GoogleDriveRole,
      sendEmailNotification: Boolean = true
  ): F[Permission] =
    retry(
      client.share(fileId, email, role, sendEmailNotification)
    )

  def findFileInSubFolderOf(
      keywords: String,
      rootId: String,
      maybeMimeType: Option[GoogleMimeType] = None
  ): F[Option[GoogleSearchResult]] = {

    val mimeTypeQueryPart = maybeMimeType.fold("")(mimeType => s" and mimeType = '${GoogleMimeType.name(mimeType)}'")
    val query             = s"name contains '$keywords'" + mimeTypeQueryPart

    listFiles(query)
      .flatMap { list =>
        val files = list.getFiles.asScalaListNotNull

        files.traverse(file => isInSubFolderOf(file.getId, rootId)).map { result =>
          (result zip files)
            .find { case (isInSubFolder, _) => isInSubFolder }
            .map { case (_, file) => GoogleSearchResult(file.getId, file.getName) }
        }
      }
  }

  def listFiles(query: String): F[FileList] =
    retry(
      client.listFiles(query)
    )

  def isInSubFolderOf(id: String, rootId: String): F[Boolean] = {

    def step(currentId: String): F[Either[String, Boolean]] =
      getParents(currentId).map {
        case Nil                                 => Right(false)
        case parents if parents.contains(rootId) => Right(true)
        case next :: _                           => Left(next)
      }

    id.tailRecM(step)
  }

  def getParents(id: String): F[List[String]] =
    retry(
      client.getParents(id)
    )

  def downloadAsInputStream(fileId: String): F[InputStream] =
    retry(
      client.downloadAsInputStream(fileId)
    )
}

object GoogleDriveClientRetry {

  def apply[F[_]: Sync](
      authenticator: GoogleAuthenticator
  )(implicit timer: Timer[F]): GoogleDriveClientRetry[F] =
    new GoogleDriveClientRetry(authenticator, Retry.defaultPolicy[F], Retry.defaultOnError[F])
}
