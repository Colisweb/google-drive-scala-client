package com.colisweb.gdrive.client.drive

import com.colisweb.gdrive.client.GoogleUtilities._
import com.colisweb.gdrive.client._
import com.colisweb.gdrive.client.drive.GoogleDriveRole.GoogleDriveRole
import com.google.api.client.http.FileContent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.{FileList, Permission, File => DriveFile}
import com.google.auth.http.HttpCredentialsAdapter

import java.io.{File, InputStream}
import java.util.Collections
import scala.annotation.tailrec
import scala.util.chaining._

class GoogleDriveClient(authenticator: GoogleAuthenticator) {

  private val service =
    new Drive.Builder(
      authenticator.httpTransport,
      authenticator.jsonFactory,
      new HttpCredentialsAdapter(authenticator.credentials)
    ).pipe(builder => authenticator.applicationName.fold(builder)(builder.setApplicationName))
      .build()

  def uploadFile(
      folderId: String,
      file: File,
      driveFilename: String,
      filetype: GoogleMimeType,
      outputFiletype: Option[GoogleMimeType] // possibility to convert the file to a google workspace file type
  ): String = {
    val driveFileMetadata =
      new DriveFile()
        .setName(driveFilename)
        .setMimeType(GoogleMimeType.name(outputFiletype.getOrElse(filetype)))
        .setParents(Collections.singletonList(folderId))

    val content = new FileContent(GoogleMimeType.name(filetype), file)

    service.files
      .create(driveFileMetadata, content)
      .setSupportsAllDrives(true)
      .setFields("id")
      .execute
      .getId
  }

  def delete(fileId: String): Unit = {
    service
      .files()
      .delete(fileId)
      .setSupportsAllDrives(true)
      .execute()

    ()
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
      .setSupportsAllDrives(true)
      .setIncludeItemsFromAllDrives(true)
      .execute()
      .getFiles
      .asScalaListNotNull
      .map { file =>
        val name = file.getName
        val id   = file.getId
        GoogleSearchResult(id, name)
      }
  }

  def share(fileId: String, email: String, role: GoogleDriveRole, sendEmailNotification: Boolean = true): Permission =
    service
      .permissions()
      .create(fileId, (new Permission).setEmailAddress(email).setType("user").setRole(role.toString))
      .setSendNotificationEmail(sendEmailNotification)
      .execute()

  def createFolder(name: String, parentId: Option[String] = None): String = {
    val folderMetadata =
      new DriveFile()
        .setName(name)
        .setMimeType(GoogleMimeType.driveFolder)

    val create = parentId match {
      case Some(parent) =>
        service.files
          .create(folderMetadata.setParents(Collections.singletonList(parent)))
          .setFields("id, parents")
      case None =>
        service.files
          .create(folderMetadata)
          .setFields("id")
    }
    create.setSupportsAllDrives(true).execute.getId

  }

  def move(targetId: String, parentId: String): Boolean = {
    val driveFile =
      service
        .files()
        .get(targetId)
        .setFields("parents")
        .execute()

    val previousParents = driveFile.getParents.asScalaListNotNull.mkString(",")

    val updatedFile =
      service
        .files()
        .update(targetId, null) // null means we don't update the content of the file
        .setRemoveParents(previousParents)
        .setAddParents(parentId)
        .setFields("id, parents")
        .setSupportsAllDrives(true)
        .execute()

    updatedFile.getParents.asScalaListNotNull
      .mkString("") == parentId
  }

  def getParents(id: String): List[String] =
    Option(
      service
        .files()
        .get(id)
        .setFields("id, parents")
        .execute()
        .getParents
    ).map(_.asScalaListNotNull).getOrElse(Nil)

  @tailrec
  final def isInSubFolderOf(id: String, rootId: String): Boolean =
    getParents(id) match {
      case Nil                                 => false
      case parents if parents.contains(rootId) => true
      case next :: _                           => isInSubFolderOf(next, rootId)
    }

  def listFiles(query: String): FileList =
    service
      .files()
      .list()
      .setQ(query)
      .setSpaces("drive")
      .setFields("files(id, name, parents)")
      .execute()

  def findFileInSubFolderOf(
      keywords: String,
      rootId: String,
      maybeMimeType: Option[GoogleMimeType] = None
  ): Option[GoogleSearchResult] = {

    val mimeTypeQueryPart = maybeMimeType.fold("")(mimeType => s" and mimeType = '${GoogleMimeType.name(mimeType)}'")
    val query             = s"name contains '$keywords'" + mimeTypeQueryPart

    listFiles(query).getFiles.asScalaListNotNull
      .find(file => isInSubFolderOf(file.getId, rootId))
      .map(file => GoogleSearchResult(file.getId, file.getName))
  }

  def downloadAsInputStream(fileId: String): InputStream =
    service
      .files()
      .get(fileId)
      .executeMediaAsInputStream()

}

final case class GoogleSearchResult(id: String, name: String)

object GoogleDriveRole extends Enumeration {
  type GoogleDriveRole = Value
  val owner: GoogleDriveRole         = Value("owner")
  val organizer: GoogleDriveRole     = Value("organizer")
  val fileOrganizer: GoogleDriveRole = Value("fileOrganizer")
  val writer: GoogleDriveRole        = Value("writer")
  val commenter: GoogleDriveRole     = Value("commenter")
}
