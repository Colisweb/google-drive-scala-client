package com.colisweb.cats.gdrive.client

import cats.effect.{Sync, Timer}
import com.colisweb.gdrive.client.{GoogleAuthenticator, GoogleSpreadsheet}
import retry.{RetryDetails, RetryPolicy}

object GoogleSpreadsheetSync {

  def createWithSheets[F[_]: Sync](
      authenticator: GoogleAuthenticator,
      spreadSheetTitle: String,
      titles: List[String],
      retryPolicy: RetryPolicy[F],
      onError: (Throwable, RetryDetails) => F[Unit]
  )(implicit timer: Timer[F]): F[GoogleSpreadsheet] =
    Retry.retry[F, GoogleSpreadsheet](
      policy = retryPolicy,
      onError = onError
    )(
      GoogleSpreadsheet.createWithSheets(authenticator, spreadSheetTitle, titles)
    )

  def createWithSheets[F[_]: Sync](
      authenticator: GoogleAuthenticator,
      spreadSheetTitle: String,
      titles: List[String]
  )(implicit timer: Timer[F]): F[GoogleSpreadsheet] =
    Retry.retry[F, GoogleSpreadsheet](
      policy = Retry.defaultPolicy,
      onError = Retry.defaultOnError
    )(
      GoogleSpreadsheet.createWithSheets(authenticator, spreadSheetTitle, titles)
    )
}
