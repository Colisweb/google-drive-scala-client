package com.colisweb.cats.gdrive.client

import java.io.File

import cats.effect.Sync
import com.colisweb.gdrive.client.{GoogleDriveApiClient, GoogleUploader}

class GoogleUploaderCats[F[_]](driveClient: GoogleDriveApiClient)(implicit F: Sync[F]) {

  private val uploader = new GoogleUploader(driveClient)

  def uploadDirectoryTo(parentId: String, destinationPath: List[String], localFolder: File): F[String] =
    F.delay(
      uploader.uploadDirectoryTo(parentId, destinationPath, localFolder)
    )

}
