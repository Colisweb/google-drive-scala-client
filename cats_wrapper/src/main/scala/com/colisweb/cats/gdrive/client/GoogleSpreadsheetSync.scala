package com.colisweb.cats.gdrive.client

import cats.effect.{Sync, Timer}
import com.colisweb.gdrive.client.{GoogleAuthenticator, GoogleSpreadsheet}
import retry.RetryPolicy

object GoogleSpreadsheetSync {

  def createWithSheets[F[_]](
      authenticator: GoogleAuthenticator,
      spreadSheetTitle: String,
      titles: List[String],
      retryPolicy: RetryPolicy[F]
  )(implicit F: Sync[F], timer: Timer[F]): F[GoogleSpreadsheet] =
    Retry.retry[F, GoogleSpreadsheet](retryPolicy)(
      GoogleSpreadsheet.createWithSheets(authenticator, spreadSheetTitle, titles)
    )
}
