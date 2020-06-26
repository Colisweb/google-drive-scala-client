package com.colisweb.cats.gdrive.client

import java.io.File

import cats.effect.{Sync, Timer}
import cats.implicits._
import com.colisweb.gdrive.client.GoogleDriveRole.GoogleDriveRole
import com.colisweb.gdrive.client.{
  CsvFileNotFound,
  CsvFileType,
  FileNotFound,
  FolderNotFound,
  GoogleAuthenticator,
  GoogleDriveClient,
  GoogleDriveFolderType,
  GoogleError,
  GoogleMimeType,
  GoogleSearchResult,
  GoogleSpreadsheetType,
  SpreadsheetNotFound
}
import com.google.api.services.drive.model.{FileList, Permission}
import retry._

import scala.jdk.CollectionConverters._

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

  def searchWithinFolder(
      keywords: String,
      rootId: String,
      maybeMimeType: Option[GoogleMimeType] = None
  ): F[Either[GoogleError, GoogleSearchResult]] = {

    def optionToError(maybeResult: Option[GoogleSearchResult]): Either[GoogleError, GoogleSearchResult] =
      maybeResult.toRight {
        maybeMimeType match {
          case Some(GoogleSpreadsheetType) => SpreadsheetNotFound(keywords)
          case Some(GoogleDriveFolderType) => FolderNotFound(keywords)
          case Some(CsvFileType)           => CsvFileNotFound(keywords)
          case None                        => FileNotFound(keywords)
        }
      }

    val mimeTypeQueryPart = maybeMimeType.fold("")(mimeType => s" and mimeType = '${GoogleMimeType.name(mimeType)}'")
    val query             = s"name contains '$keywords'" + mimeTypeQueryPart

    listFiles(query)
      .flatMap { list =>
        val files = list.getFiles.asScala.toList

        files.traverse(file => isInSubFolderOf(file.getId, rootId)).map { result =>
          (result zip files)
            .find { case (isInSubFolder, _) => isInSubFolder }
            .map { case (_, file) => GoogleSearchResult(file.getId, file.getName) }
        }
      }
      .map(optionToError)
  }
}

object GoogleDriveClientSync {

  def apply[F[_]: Sync](
      authenticator: GoogleAuthenticator
  )(implicit timer: Timer[F]): GoogleDriveClientSync[F] =
    new GoogleDriveClientSync(authenticator, Retry.defaultPolicy[F], Retry.defaultOnError[F])
}
