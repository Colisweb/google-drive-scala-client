package com.colisweb.cats.gdrive.client

import cats.effect.{Sync, Timer}
import cats.implicits._
import com.colisweb.gdrive.client.GoogleAuthenticator
import com.colisweb.gdrive.client.sheets.{
  GoogleBatchRequest,
  GoogleSheetClient,
  GoogleSheetProperties,
  InputOption,
  InputOptionRaw,
  SheetRangeContent
}
import com.google.api.services.sheets.v4.model.{BatchUpdateSpreadsheetResponse, RowData, SheetProperties}
import retry.{RetryDetails, RetryPolicy}

class GoogleSheetClientRetry[F[_]](
    authenticator: GoogleAuthenticator,
    retryPolicy: RetryPolicy[F],
    onError: (Throwable, RetryDetails) => F[Unit]
)(implicit
    timer: Timer[F],
    S: Sync[F]
) extends Retry[F](retryPolicy, onError) {

  val client = new GoogleSheetClient(authenticator)

  def createSpreadsheet(name: String, sheetsProperties: List[GoogleSheetProperties]): F[String] =
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

  def retrieveSheetsProperties(id: String): F[List[SheetProperties]] =
    retry(
      client.retrieveSheetsProperties(id)
    )

  def batchRequests(spreadsheetId: String, requests: List[GoogleBatchRequest]): F[Unit] =
    retry(
      client.batchRequests(spreadsheetId, requests)
    ) *> S.unit

  def batchRequestsWithResponse(
      spreadsheetId: String,
      requests: List[GoogleBatchRequest]
  ): F[BatchUpdateSpreadsheetResponse] =
    retry(
      client.batchRequests(spreadsheetId, requests)
    )
}

object GoogleSheetClientRetry {
  def apply[F[_]: Sync](
      authenticator: GoogleAuthenticator
  )(implicit timer: Timer[F]): GoogleSheetClientRetry[F] =
    new GoogleSheetClientRetry(authenticator, Retry.defaultPolicy, Retry.defaultOnError[F])
}
