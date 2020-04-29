package com.colisweb.cats.gdrive.client

import cats.effect.{Sync, Timer}
import com.colisweb.gdrive.client.{GoogleAuthenticator, GoogleSheetClient, GoogleSheet}
import com.google.api.services.sheets.v4.model.RowData
import retry.{RetryDetails, RetryPolicy}

class GoogleSheetClientSync[F[_]: Sync](
    authenticator: GoogleAuthenticator,
    retryPolicy: RetryPolicy[F],
    onError: (Throwable, RetryDetails) => F[Unit]
)(
    implicit timer: Timer[F]
) extends Retry[F](retryPolicy, onError) {

  val client = new GoogleSheetClient(authenticator)

  def createSpreadsheet(name: String, sheetsTitles: List[String]): F[String] =
    retry(
      client.createSpreadsheet(name, sheetsTitles)
    )

  def maybeReadSpreadsheet[T](
      id: String,
      ranges: List[String],
      parseFields: List[List[String]] => List[T]
  ): F[List[T]] =
    retry(
      client.maybeReadSpreadsheet(id, ranges, parseFields)
    )

  def readRows(id: String, range: String): F[Seq[RowData]] =
    retry(
      client.readRows(id, range)
    )

  def readRows(id: String, ranges: List[String]): F[Seq[Seq[RowData]]] =
    retry(
      client.readRows(id, ranges)
    )

  def writeRanges(id: String, sheets: List[GoogleSheet]): F[Unit] =
    retry(
      client.writeRanges(id, sheets)
    )

  def writeRange(id: String, range: String, content: Seq[Seq[AnyRef]]): F[Unit] =
    retry(
      client.writeRange(id, range, content)
    )

  def retrieveSheetsIds(id: String): F[Map[String, Int]] =
    retry(
      client.retrieveSheetsIds(id)
    )
}
