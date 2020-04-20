package com.colisweb.gdrive.client

import java.io.File

import com.colisweb.gdrive.client.GoogleDriveRole.GoogleDriveRole
import com.google.api.client.http.FileContent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.{Permission, File => DriveFile}

import scala.collection.JavaConverters._

case class GoogleDriveApiClient(driveService: Drive) {

  def uploadTo(
      driveFileName: String,
      fileToUpload: File,
      destinationFolderId: String,
      targetMimeType: String,
      outputMimeType: Option[String] = None
  ): Unit = {
    val fileId = upload(driveFileName, fileToUpload, targetMimeType, outputMimeType)
    move(fileId, destinationFolderId)
  }

  def createFolderTo(parentId: String, name: String): String = {
    val folderId = createFolder(name)
    move(folderId, parentId)
    folderId
  }

  def delete(fileId: String): Unit =
    driveService.files().delete(fileId).execute()

  // The files must be shared with the service account for it to search them.
  def listFilesInFolder(folderId: String): List[GoogleSearchResult] = {
    val query = s"'$folderId' in parents"

    driveService
      .files()
      .list()
      .setQ(query)
      .setSpaces("drive")
      .setFields("files(id, name)")
      .execute()
      .getFiles
      .asScala
      .toList
      .map { file =>
        val name = file.getName
        val id   = file.getId
        GoogleSearchResult(id, name)
      }

  }

  def share(fileId: String, email: String, role: GoogleDriveRole): Permission =
    driveService
      .permissions()
      .create(fileId, (new Permission).setEmailAddress(email).setType("user").setRole(role.toString))
      .execute()

  private def upload(
      driveFileName: String,
      fileToUpload: File,
      targetMimeType: String,
      outputMimeType: Option[String]
  ): String = {
    val driveFileMetadata =
      new DriveFile()
        .setName(driveFileName)
        .setMimeType(outputMimeType.getOrElse(targetMimeType))

    val content = new FileContent(targetMimeType, fileToUpload)

    driveService.files
      .create(driveFileMetadata, content)
      .setFields("id")
      .execute
      .getId

  }

  private def createFolder(name: String): String = {
    val folderMetadata =
      new DriveFile()
        .setName(name)
        .setMimeType(GoogleMimeType.driveFolder)

    driveService.files
      .create(folderMetadata)
      .setFields("id")
      .execute
      .getId
  }

  def move(targetId: String, parentId: String): Boolean = {
    val driveFile =
      driveService
        .files()
        .get(targetId)
        .setFields("parents")
        .execute()

    val previousParents = driveFile.getParents.asScala.mkString(",")

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

final case class GoogleSearchResult(id: String, name: String)

object GoogleDriveRole extends Enumeration {
  type GoogleDriveRole = Value
  val owner         = Value("owner")
  val organizer     = Value("organizer")
  val fileOrganizer = Value("fileOrganizer")
  val writer        = Value("writer")
  val commenter     = Value("commenter")
}
