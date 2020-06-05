package com.colisweb.gdrive.client

import java.io.File

import com.colisweb.gdrive.client.GoogleDriveRole.GoogleDriveRole
import com.google.api.client.http.FileContent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.{Permission, File => DriveFile}
import com.google.auth.http.HttpCredentialsAdapter

import scala.jdk.CollectionConverters._

class GoogleDriveClient(authenticator: GoogleAuthenticator) {

  private val service =
    new Drive.Builder(
      authenticator.httpTransport,
      authenticator.jsonFactory,
      new HttpCredentialsAdapter(authenticator.credentials)
    ).setApplicationName(authenticator.applicationName)
      .build()

  def uploadFileTo(
      folderId: String,
      file: File,
      driveFilename: String,
      filetype: GoogleMimeType
  ): String = {
    val fileId = uploadFile(file, driveFilename, filetype)
    move(fileId, folderId)
    fileId
  }

  def createFolderTo(parentId: String, name: String): String = {
    val folderId = createFolder(name)
    move(folderId, parentId)
    folderId
  }

  def delete(fileId: String) = {
    service
      .files()
      .delete(fileId)
      .execute()
  }

  // The files must be shared with the service account for it to search them.
  def listFilesInFolder(folderId: String): List[GoogleSearchResult] = {
    val query = s"'$folderId' in parents"

    service
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
    service
      .permissions()
      .create(fileId, (new Permission).setEmailAddress(email).setType("user").setRole(role.toString))
      .execute()

  def uploadFile(
      file: File,
      driveFilename: String,
      filetype: GoogleMimeType
  ): String = {
    val filetypeName = GoogleMimeType.name(filetype)
    val driveFileMetadata =
      new DriveFile()
        .setName(driveFilename)
        .setMimeType(filetypeName)

    val content = new FileContent(filetypeName, file)

    service.files
      .create(driveFileMetadata, content)
      .setFields("id")
      .execute
      .getId

  }

  def createFolder(name: String): String = {
    val folderMetadata =
      new DriveFile()
        .setName(name)
        .setMimeType(GoogleMimeType.driveFolder)

    service.files
      .create(folderMetadata)
      .setFields("id")
      .execute
      .getId
  }

  def move(targetId: String, parentId: String): Boolean = {
    val driveFile =
      service
        .files()
        .get(targetId)
        .setFields("parents")
        .execute()

    val previousParents = driveFile.getParents.asScala.mkString(",")

    val updatedFile =
      service
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
