package com.colisweb.gdrive.client

import java.io.File

import cats.effect.Sync
import cats.implicits._
import com.google.api.client.http.FileContent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.{FileList, File => DriveFile}
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

case class GoogleDriveApiClient[F[_]](driveService: Drive)(implicit F: Sync[F]) {

  private val log = LoggerFactory.getLogger(classOf[GoogleDriveApiClient[F]])

  def uploadTo(
      driveFileName: String,
      fileToUpload: File,
      destinationFolderId: String,
      targetMimeType: String,
      outputMimeType: Option[String] = None
  ): F[Unit] =
    for {
      fileId <- upload(driveFileName, fileToUpload, targetMimeType, outputMimeType)
      _      <- move(fileId, destinationFolderId)
    } yield ()

  def createFolderTo(parentId: String, name: String): F[String] =
    for {
      folderId <- createFolder(name)
      _        <- move(folderId, parentId)
    } yield folderId

  // The files must be shared with the service account for it to search them.
  def listFilesInFolder(folderId: String): F[List[GoogleSearchResult]] = {
    val query = s"'$folderId' in parents"

    log.debug(s"Listing file from folder with id $folderId")

    val result: F[FileList] = F.delay {
      driveService
        .files()
        .list()
        .setQ(query)
        .setSpaces("drive")
        .setFields("files(id, name)")
        .execute()
    }

    result.map { files =>
      files.getFiles.asScala.toList.map { file =>
        val name = file.getName
        val id   = file.getId
        log.debug(s"Found $name with id $id")
        GoogleSearchResult(id, name)
      }
    }
  }

  private def upload(
      driveFileName: String,
      fileToUpload: File,
      targetMimeType: String,
      outputMimeType: Option[String]
  ): F[String] = {
    F.delay {
        val driveFileMetadata =
          new DriveFile()
            .setName(driveFileName)
            .setMimeType(outputMimeType.getOrElse(targetMimeType))

        val content = new FileContent(targetMimeType, fileToUpload)

        log.debug("Uploading file to Drive")

        driveService.files
          .create(driveFileMetadata, content)
          .setFields("id")
          .execute
      }
      .map(_.getId)
  }

  private def createFolder(name: String): F[String] = {
    F.delay {
        val folderMetadata =
          new DriveFile()
            .setName(name)
            .setMimeType(GoogleMimeType.driveFolder)

        log.debug(s"Creating folder with name $name")
        driveService.files
          .create(folderMetadata)
          .setFields("id")
          .execute
      }
      .map(_.getId)
  }

  private def move(targetId: String, parentId: String): F[Boolean] = {
    F.delay {
      val driveFile =
        driveService
          .files()
          .get(targetId)
          .setFields("parents")
          .execute()

      val previousParents = driveFile.getParents.asScala.mkString(",")

      log.debug(s"Moving $targetId to folder $parentId")

      val updatedFile =
        driveService
          .files()
          .update(targetId, null) // null means we don't update the content of the file
          .setRemoveParents(previousParents)
          .setAddParents(parentId)
          .setFields("id, parents")
          .execute()

      updatedFile.getParents.asScala
        .mkString("") == parentId
    }
  }
}

final case class GoogleSearchResult(id: String, name: String)
