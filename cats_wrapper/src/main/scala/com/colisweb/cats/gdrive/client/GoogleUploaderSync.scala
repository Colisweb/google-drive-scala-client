package com.colisweb.cats.gdrive.client

import java.io.File

import cats.effect.{Sync, Timer}
import com.colisweb.gdrive.client.{GoogleDriveClient, GoogleUploader}
import retry.RetryPolicy

class GoogleUploaderSync[F[_]](driveClient: GoogleDriveClient, retryPolicy: RetryPolicy[F])(
    implicit F: Sync[F],
    timer: Timer[F]
) extends Retry[F](retryPolicy) {

  private val uploader = new GoogleUploader(driveClient)

  def uploadDirectoryTo(parentId: String, destinationPath: List[String], localFolder: File): F[String] =
    retry(
      uploader.uploadDirectoryTo(parentId, destinationPath, localFolder)
    )

}
