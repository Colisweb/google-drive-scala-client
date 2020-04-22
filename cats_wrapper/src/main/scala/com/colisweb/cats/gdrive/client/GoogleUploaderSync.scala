package com.colisweb.cats.gdrive.client

import java.io.File

import cats.effect.{Sync, Timer}
import com.colisweb.gdrive.client.{GoogleDriveClient, GoogleUploader}
import retry.{RetryDetails, RetryPolicy}

class GoogleUploaderSync[F[_]: Sync](
    driveClient: GoogleDriveClient,
    retryPolicy: RetryPolicy[F],
    onError: (Throwable, RetryDetails) => F[Unit]
)(
    implicit timer: Timer[F]
) extends Retry[F](retryPolicy, onError) {

  private val uploader = new GoogleUploader(driveClient)

  def uploadDirectoryTo(parentId: String, destinationPath: List[String], localFolder: File): F[String] =
    retry(
      uploader.uploadDirectoryTo(parentId, destinationPath, localFolder)
    )

}

object GoogleUploaderSync {

  def apply[F[_]: Sync](
      driveClient: GoogleDriveClient,
      retryPolicy: RetryPolicy[F],
      onError: (Throwable, RetryDetails) => F[Unit]
  )(implicit timer: Timer[F]): GoogleUploaderSync[F] =
    new GoogleUploaderSync(driveClient, retryPolicy, onError)

  def apply[F[_]: Sync](
      driveClient: GoogleDriveClient
  )(implicit timer: Timer[F]): GoogleUploaderSync[F] =
    new GoogleUploaderSync(driveClient, Retry.defaultPolicy[F], Retry.defaultOnError[F])
}
