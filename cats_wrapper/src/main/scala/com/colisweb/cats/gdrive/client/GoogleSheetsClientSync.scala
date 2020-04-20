package com.colisweb.cats.gdrive.client

import cats.effect.Sync
import com.colisweb.gdrive.client.{GoogleAuthenticator, GoogleSheetsClient}

class GoogleSheetsClientSync[F[_]](authenticator: GoogleAuthenticator)(implicit F: Sync[F]) {

  private val client = new GoogleSheetsClient(authenticator)

  def createWithSheets(spreadSheetTitle: String, titles: List[String]): F[client.GoogleSpreadsheet] =
    F.delay(
      client.createWithSheets(spreadSheetTitle, titles)
    )
}
