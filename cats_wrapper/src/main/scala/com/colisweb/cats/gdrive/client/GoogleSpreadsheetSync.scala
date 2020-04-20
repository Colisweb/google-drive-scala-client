package com.colisweb.cats.gdrive.client

import cats.effect.Sync
import com.colisweb.gdrive.client.{GoogleAuthenticator, GoogleSpreadsheet}

object GoogleSpreadsheetSync {

  def createWithSheets[F[_]](
      authenticator: GoogleAuthenticator,
      spreadSheetTitle: String,
      titles: List[String]
  )(implicit F: Sync[F]): F[GoogleSpreadsheet] =
    F.delay(
      GoogleSpreadsheet.createWithSheets(authenticator, spreadSheetTitle, titles)
    )
}
