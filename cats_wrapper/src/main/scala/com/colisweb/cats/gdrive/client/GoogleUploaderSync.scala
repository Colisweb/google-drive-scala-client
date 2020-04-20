package com.colisweb.cats.gdrive.client

import java.io.File

import cats.effect.Sync
import com.colisweb.gdrive.client.{GoogleDriveClient, GoogleUploader}

class GoogleUploaderSync[F[_]](driveClient: GoogleDriveClient)(implicit F: Sync[F]) {

  private val uploader = new GoogleUploader(driveClient)

  def uploadDirectoryTo(parentId: String, destinationPath: List[String], localFolder: File): F[String] =
    F.delay(
      uploader.uploadDirectoryTo(parentId, destinationPath, localFolder)
    )

}
