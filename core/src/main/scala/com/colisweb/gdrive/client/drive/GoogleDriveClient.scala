package com.colisweb.gdrive.client.drive

import java.io.{File, InputStream}
import com.colisweb.gdrive.client._
import com.colisweb.gdrive.client.drive.GoogleDriveRole.GoogleDriveRole
import com.google.api.client.http.FileContent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.{FileList, Permission, File => DriveFile}
import com.google.auth.http.HttpCredentialsAdapter

import scala.annotation.tailrec
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
      filetype: GoogleMimeType,
      outputFiletype: Option[GoogleMimeType] // possibility to convert the file to a google workspace file type
  ): String = {
    val fileId = uploadFile(file, driveFilename, filetype, outputFiletype)
    move(fileId, folderId)
    fileId
  }

  def createFolderTo(parentId: String, name: String): String = {
    val folderId = createFolder(name)
    move(folderId, parentId)
    folderId
  }

  def delete(fileId: String): Unit = {
    service
      .files()
      .delete(fileId)
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
      filetype: GoogleMimeType,
      outputFiletype: Option[GoogleMimeType]
  ): String = {
    val driveFileMetadata =
      new DriveFile()
        .setName(driveFilename)
        .setMimeType(GoogleMimeType.name(outputFiletype.getOrElse(filetype)))

    val content = new FileContent(GoogleMimeType.name(filetype), file)

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

  def getParents(id: String): List[String] =
    Option(
      service
        .files()
        .get(id)
        .setFields("id, parents")
        .execute()
        .getParents
    ).map(_.asScala.toList).getOrElse(Nil)

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

    listFiles(query).getFiles.asScala.toList
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
