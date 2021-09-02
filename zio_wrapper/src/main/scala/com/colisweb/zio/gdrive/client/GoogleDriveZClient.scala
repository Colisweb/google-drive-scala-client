package com.colisweb.zio.gdrive.client

import com.colisweb.gdrive.client.GoogleAuthenticator
import com.colisweb.gdrive.client.GoogleUtilities._
import com.colisweb.gdrive.client.drive.GoogleDriveRole.GoogleDriveRole
import com.colisweb.gdrive.client.drive.{GoogleDriveClient, GoogleMimeType, GoogleSearchResult}
import com.google.api.services.drive.model.{FileList, Permission}
import zio.clock.Clock
import zio.{RIO, Schedule, ZIO}

import java.io.{File, InputStream}

class GoogleDriveZClient(
    authenticator: GoogleAuthenticator,
    retryPolicy: Schedule[Any, Throwable, Any] = RetryPolicies.default
) {

  private val retry  = new Retry(retryPolicy)
  private val client = new GoogleDriveClient(authenticator)

  def uploadFileTo(
      folderId: String,
      file: File,
      driveFilename: String,
      filetype: GoogleMimeType,
      outputFiletype: Option[GoogleMimeType]
  ): RIO[Clock, String] =
    retry(
      client.uploadFileTo(folderId, file, driveFilename, filetype, outputFiletype)
    )

  def createFolderTo(parentId: String, name: String): RIO[Clock, String] =
    retry(
      client.createFolderTo(parentId, name)
    )

  def delete(fileId: String): RIO[Clock, Unit] =
    retry(
      client.delete(fileId)
    ).unit

  def listFilesInFolder(folderId: String): RIO[Clock, List[GoogleSearchResult]] =
    retry(
      client.listFilesInFolder(folderId)
    )

  def uploadFile(
      file: File,
      driveFilename: String,
      filetype: GoogleMimeType,
      outputFiletype: Option[GoogleMimeType]
  ): RIO[Clock, String] =
    retry(
      client.uploadFile(file, driveFilename, filetype, outputFiletype)
    )

  def createFolder(name: String): RIO[Clock, String] =
    retry(
      client.createFolder(name)
    )

  def move(targetId: String, parentId: String): RIO[Clock, Unit] =
    retry(
      client.move(targetId, parentId)
    ).unit

  def share(fileId: String, email: String, role: GoogleDriveRole): RIO[Clock, Permission] =
    retry(
      client.share(fileId, email, role)
    )

  def getParents(id: String): RIO[Clock, List[String]] =
    retry(
      client.getParents(id)
    )

  def listFiles(query: String): RIO[Clock, FileList] =
    retry(
      client.listFiles(query)
    )

  def isInSubFolderOf(id: String, rootId: String): RIO[Clock, Boolean] = {

    def step(currentId: String): RIO[Clock, Boolean] =
      getParents(currentId).flatMap {
        case Nil                                 => ZIO.succeed(false)
        case parents if parents.contains(rootId) => ZIO.succeed(true)
        case next :: _                           => step(next)
      }
    step(id)
  }

  def findFileInSubFolderOf(
      keywords: String,
      rootId: String,
      maybeMimeType: Option[GoogleMimeType] = None
  ): RIO[Clock, Option[GoogleSearchResult]] = {

    val mimeTypeQueryPart = maybeMimeType.fold("")(mimeType => s" and mimeType = '${GoogleMimeType.name(mimeType)}'")
    val query             = s"name contains '$keywords'" + mimeTypeQueryPart

    listFiles(query)
      .flatMap { list =>
        val files = list.getFiles.asScalaListNotNull

        ZIO.foreach(files)(file => isInSubFolderOf(file.getId, rootId)).map { result =>
          (result zip files)
            .find { case (isInSubFolder, _) => isInSubFolder }
            .map { case (_, file) => GoogleSearchResult(file.getId, file.getName) }
        }
      }
  }

  def downloadAsInputStream(fileId: String): RIO[Clock, InputStream] =
    retry(
      client.downloadAsInputStream(fileId)
    )
}
