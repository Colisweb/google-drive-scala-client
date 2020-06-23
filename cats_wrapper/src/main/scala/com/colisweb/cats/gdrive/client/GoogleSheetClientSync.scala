package com.colisweb.cats.gdrive.client

import cats.effect.{Sync, Timer}
import cats.implicits._
import com.colisweb.gdrive.client.{
  GoogleAuthenticator,
  GoogleSheetClient,
  SheetProperties,
  InputOption,
  InputOptionRaw,
  SheetRangeContent
}
import com.google.api.services.sheets.v4.model.RowData
import retry.{RetryDetails, RetryPolicy}

class GoogleSheetClientSync[F[_]](
    authenticator: GoogleAuthenticator,
    retryPolicy: RetryPolicy[F],
    onError: (Throwable, RetryDetails) => F[Unit]
)(implicit
    timer: Timer[F],
    S: Sync[F]
) extends Retry[F](retryPolicy, onError) {

  val client = new GoogleSheetClient(authenticator)

  def createSpreadsheet(name: String, sheetsProperties: List[SheetProperties]): F[String] =
    retry(
      client.createSpreadsheet(name, sheetsProperties)
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

  def writeRanges(id: String, sheets: List[SheetRangeContent], inputOption: InputOption = InputOptionRaw): F[Unit] =
    retry(
      client.writeRanges(id, sheets, inputOption)
    ) *> S.unit

  def writeRange(id: String, sheet: SheetRangeContent, inputOption: InputOption = InputOptionRaw): F[Unit] =
    retry(
      client.writeRange(id, sheet, inputOption)
    ) *> S.unit

  def retrieveSheetsIds(id: String): F[Map[String, Int]] =
    retry(
      client.retrieveSheetsIds(id)
    )
}

object GoogleSheetClientSync {
  def apply[F[_]: Sync](
      authenticator: GoogleAuthenticator
  )(implicit timer: Timer[F]): GoogleSheetClientSync[F] =
    new GoogleSheetClientSync(authenticator, Retry.defaultPolicy, Retry.defaultOnError[F])
}
